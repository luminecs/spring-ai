package org.springframework.ai.vectorstore.redis;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.RediSearchUtil;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class RedisVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	public static final String DEFAULT_INDEX_NAME = "spring-ai-index";

	public static final String DEFAULT_CONTENT_FIELD_NAME = "content";

	public static final String DEFAULT_EMBEDDING_FIELD_NAME = "embedding";

	public static final String DEFAULT_PREFIX = "embedding:";

	public static final Algorithm DEFAULT_VECTOR_ALGORITHM = Algorithm.HSNW;

	public static final String DISTANCE_FIELD_NAME = "vector_score";

	private static final String QUERY_FORMAT = "%s=>[KNN %s @%s $%s AS %s]";

	private static final Path2 JSON_SET_PATH = Path2.of("$");

	private static final String JSON_PATH_PREFIX = "$.";

	private static final Logger logger = LoggerFactory.getLogger(RedisVectorStore.class);

	private static final Predicate<Object> RESPONSE_OK = Predicate.isEqual("OK");

	private static final Predicate<Object> RESPONSE_DEL_OK = Predicate.isEqual(1L);

	private static final String VECTOR_TYPE_FLOAT32 = "FLOAT32";

	private static final String EMBEDDING_PARAM_NAME = "BLOB";

	private static final String DEFAULT_DISTANCE_METRIC = "COSINE";

	private final JedisPooled jedis;

	private final boolean initializeSchema;

	private final String indexName;

	private final String prefix;

	private final String contentFieldName;

	private final String embeddingFieldName;

	private final Algorithm vectorAlgorithm;

	private final List<MetadataField> metadataFields;

	private final FilterExpressionConverter filterExpressionConverter;

	protected RedisVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.jedis, "JedisPooled must not be null");

		this.jedis = builder.jedis;
		this.indexName = builder.indexName;
		this.prefix = builder.prefix;
		this.contentFieldName = builder.contentFieldName;
		this.embeddingFieldName = builder.embeddingFieldName;
		this.vectorAlgorithm = builder.vectorAlgorithm;
		this.metadataFields = builder.metadataFields;
		this.initializeSchema = builder.initializeSchema;
		this.filterExpressionConverter = new RedisFilterExpressionConverter(this.metadataFields);
	}

	public JedisPooled getJedis() {
		return this.jedis;
	}

	@Override
	public void doAdd(List<Document> documents) {
		try (Pipeline pipeline = this.jedis.pipelined()) {

			List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
					this.batchingStrategy);

			for (Document document : documents) {
				var fields = new HashMap<String, Object>();
				fields.put(this.embeddingFieldName, embeddings.get(documents.indexOf(document)));
				fields.put(this.contentFieldName, document.getText());
				fields.putAll(document.getMetadata());
				pipeline.jsonSetWithEscape(key(document.getId()), JSON_SET_PATH, fields);
			}
			List<Object> responses = pipeline.syncAndReturnAll();
			Optional<Object> errResponse = responses.stream().filter(Predicate.not(RESPONSE_OK)).findAny();
			if (errResponse.isPresent()) {
				String message = MessageFormat.format("Could not add document: {0}", errResponse.get());
				if (logger.isErrorEnabled()) {
					logger.error(message);
				}
				throw new RuntimeException(message);
			}
		}
	}

	private String key(String id) {
		return this.prefix + id;
	}

	@Override
	public void doDelete(List<String> idList) {
		try (Pipeline pipeline = this.jedis.pipelined()) {
			for (String id : idList) {
				pipeline.jsonDel(key(id));
			}
			List<Object> responses = pipeline.syncAndReturnAll();
			Optional<Object> errResponse = responses.stream().filter(Predicate.not(RESPONSE_DEL_OK)).findAny();
			if (errResponse.isPresent()) {
				if (logger.isErrorEnabled()) {
					logger.error("Could not delete document: {}", errResponse.get());
				}
			}
		}
	}

	@Override
	protected void doDelete(Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try {
			String filterStr = this.filterExpressionConverter.convertExpression(filterExpression);

			List<String> matchingIds = new ArrayList<>();
			SearchResult searchResult = this.jedis.ftSearch(this.indexName, filterStr);

			for (redis.clients.jedis.search.Document doc : searchResult.getDocuments()) {
				String docId = doc.getId();
				matchingIds.add(docId.replace(key(""), ""));

			}

			if (!matchingIds.isEmpty()) {
				try (Pipeline pipeline = this.jedis.pipelined()) {
					for (String id : matchingIds) {
						pipeline.jsonDel(key(id));
					}
					List<Object> responses = pipeline.syncAndReturnAll();
					Optional<Object> errResponse = responses.stream().filter(Predicate.not(RESPONSE_DEL_OK)).findAny();

					if (errResponse.isPresent()) {
						logger.error("Could not delete document: {}", errResponse.get());
						throw new IllegalStateException("Failed to delete some documents");
					}
				}

				logger.debug("Deleted {} documents matching filter expression", matchingIds.size());
			}
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter", e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {

		Assert.isTrue(request.getTopK() > 0, "The number of documents to be returned must be greater than zero");
		Assert.isTrue(request.getSimilarityThreshold() >= 0 && request.getSimilarityThreshold() <= 1,
				"The similarity score is bounded between 0 and 1; least to most similar respectively.");

		String filter = nativeExpressionFilter(request);

		String queryString = String.format(QUERY_FORMAT, filter, request.getTopK(), this.embeddingFieldName,
				EMBEDDING_PARAM_NAME, DISTANCE_FIELD_NAME);

		List<String> returnFields = new ArrayList<>();
		this.metadataFields.stream().map(MetadataField::name).forEach(returnFields::add);
		returnFields.add(this.embeddingFieldName);
		returnFields.add(this.contentFieldName);
		returnFields.add(DISTANCE_FIELD_NAME);
		var embedding = this.embeddingModel.embed(request.getQuery());
		Query query = new Query(queryString).addParam(EMBEDDING_PARAM_NAME, RediSearchUtil.toByteArray(embedding))
			.returnFields(returnFields.toArray(new String[0]))
			.setSortBy(DISTANCE_FIELD_NAME, true)
			.limit(0, request.getTopK())
			.dialect(2);

		SearchResult result = this.jedis.ftSearch(this.indexName, query);
		return result.getDocuments()
			.stream()
			.filter(d -> similarityScore(d) >= request.getSimilarityThreshold())
			.map(this::toDocument)
			.toList();
	}

	private Document toDocument(redis.clients.jedis.search.Document doc) {
		var id = doc.getId().substring(this.prefix.length());
		var content = doc.hasProperty(this.contentFieldName) ? doc.getString(this.contentFieldName) : "";
		Map<String, Object> metadata = this.metadataFields.stream()
			.map(MetadataField::name)
			.filter(doc::hasProperty)
			.collect(Collectors.toMap(Function.identity(), doc::getString));
		metadata.put(DISTANCE_FIELD_NAME, 1 - similarityScore(doc));
		metadata.put(DocumentMetadata.DISTANCE.value(), 1 - similarityScore(doc));
		return Document.builder().id(id).text(content).metadata(metadata).score((double) similarityScore(doc)).build();
	}

	private float similarityScore(redis.clients.jedis.search.Document doc) {
		return (2 - Float.parseFloat(doc.getString(DISTANCE_FIELD_NAME))) / 2;
	}

	private String nativeExpressionFilter(SearchRequest request) {
		if (request.getFilterExpression() == null) {
			return "*";
		}
		return "(" + this.filterExpressionConverter.convertExpression(request.getFilterExpression()) + ")";
	}

	@Override
	public void afterPropertiesSet() {

		if (!this.initializeSchema) {
			return;
		}

		if (this.jedis.ftList().contains(this.indexName)) {
			return;
		}

		String response = this.jedis.ftCreate(this.indexName,
				FTCreateParams.createParams().on(IndexDataType.JSON).addPrefix(this.prefix), schemaFields());
		if (!RESPONSE_OK.test(response)) {
			String message = MessageFormat.format("Could not create index: {0}", response);
			throw new RuntimeException(message);
		}
	}

	private Iterable<SchemaField> schemaFields() {
		Map<String, Object> vectorAttrs = new HashMap<>();
		vectorAttrs.put("DIM", this.embeddingModel.dimensions());
		vectorAttrs.put("DISTANCE_METRIC", DEFAULT_DISTANCE_METRIC);
		vectorAttrs.put("TYPE", VECTOR_TYPE_FLOAT32);
		List<SchemaField> fields = new ArrayList<>();
		fields.add(TextField.of(jsonPath(this.contentFieldName)).as(this.contentFieldName).weight(1.0));
		fields.add(VectorField.builder()
			.fieldName(jsonPath(this.embeddingFieldName))
			.algorithm(vectorAlgorithm())
			.attributes(vectorAttrs)
			.as(this.embeddingFieldName)
			.build());

		if (!CollectionUtils.isEmpty(this.metadataFields)) {
			for (MetadataField field : this.metadataFields) {
				fields.add(schemaField(field));
			}
		}
		return fields;
	}

	private SchemaField schemaField(MetadataField field) {
		String fieldName = jsonPath(field.name);
		return switch (field.fieldType) {
			case NUMERIC -> NumericField.of(fieldName).as(field.name);
			case TAG -> TagField.of(fieldName).as(field.name);
			case TEXT -> TextField.of(fieldName).as(field.name);
			default -> throw new IllegalArgumentException(
					MessageFormat.format("Field {0} has unsupported type {1}", field.name, field.fieldType));
		};
	}

	private VectorAlgorithm vectorAlgorithm() {
		if (this.vectorAlgorithm == Algorithm.HSNW) {
			return VectorAlgorithm.HNSW;
		}
		return VectorAlgorithm.FLAT;
	}

	private String jsonPath(String field) {
		return JSON_PATH_PREFIX + field;
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.REDIS.value(), operationName)
			.collectionName(this.indexName)
			.dimensions(this.embeddingModel.dimensions())
			.fieldName(this.embeddingFieldName)
			.similarityMetric(VectorStoreSimilarityMetric.COSINE.value());

	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.jedis;
		return Optional.of(client);
	}

	public static Builder builder(JedisPooled jedis, EmbeddingModel embeddingModel) {
		return new Builder(jedis, embeddingModel);
	}

	public enum Algorithm {

		FLAT, HSNW

	}

	public record MetadataField(String name, FieldType fieldType) {

		public static MetadataField text(String name) {
			return new MetadataField(name, FieldType.TEXT);
		}

		public static MetadataField numeric(String name) {
			return new MetadataField(name, FieldType.NUMERIC);
		}

		public static MetadataField tag(String name) {
			return new MetadataField(name, FieldType.TAG);
		}

	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final JedisPooled jedis;

		private String indexName = DEFAULT_INDEX_NAME;

		private String prefix = DEFAULT_PREFIX;

		private String contentFieldName = DEFAULT_CONTENT_FIELD_NAME;

		private String embeddingFieldName = DEFAULT_EMBEDDING_FIELD_NAME;

		private Algorithm vectorAlgorithm = DEFAULT_VECTOR_ALGORITHM;

		private List<MetadataField> metadataFields = new ArrayList<>();

		private boolean initializeSchema = false;

		private Builder(JedisPooled jedis, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(jedis, "JedisPooled must not be null");
			this.jedis = jedis;
		}

		public Builder indexName(String indexName) {
			if (StringUtils.hasText(indexName)) {
				this.indexName = indexName;
			}
			return this;
		}

		public Builder prefix(String prefix) {
			if (StringUtils.hasText(prefix)) {
				this.prefix = prefix;
			}
			return this;
		}

		public Builder contentFieldName(String fieldName) {
			if (StringUtils.hasText(fieldName)) {
				this.contentFieldName = fieldName;
			}
			return this;
		}

		public Builder embeddingFieldName(String fieldName) {
			if (StringUtils.hasText(fieldName)) {
				this.embeddingFieldName = fieldName;
			}
			return this;
		}

		public Builder vectorAlgorithm(@Nullable Algorithm algorithm) {
			if (algorithm != null) {
				this.vectorAlgorithm = algorithm;
			}
			return this;
		}

		public Builder metadataFields(MetadataField... fields) {
			return metadataFields(Arrays.asList(fields));
		}

		public Builder metadataFields(@Nullable List<MetadataField> fields) {
			if (fields != null && !fields.isEmpty()) {
				this.metadataFields = new ArrayList<>(fields);
			}
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		@Override
		public RedisVectorStore build() {
			return new RedisVectorStore(this);
		}

	}

}
