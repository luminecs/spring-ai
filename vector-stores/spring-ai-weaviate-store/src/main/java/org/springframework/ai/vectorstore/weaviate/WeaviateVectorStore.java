package org.springframework.ai.vectorstore.weaviate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLError;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder.GetBuilderBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.model.EmbeddingUtils;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class WeaviateVectorStore extends AbstractObservationVectorStore {

	private static final Logger logger = LoggerFactory.getLogger(WeaviateVectorStore.class);

	private static final String METADATA_FIELD_PREFIX = "meta_";

	private static final String CONTENT_FIELD_NAME = "content";

	private static final String METADATA_FIELD_NAME = "metadata";

	private static final String ADDITIONAL_FIELD_NAME = "_additional";

	private static final String ADDITIONAL_ID_FIELD_NAME = "id";

	private static final String ADDITIONAL_CERTAINTY_FIELD_NAME = "certainty";

	private static final String ADDITIONAL_VECTOR_FIELD_NAME = "vector";

	private final WeaviateClient weaviateClient;

	private final ConsistentLevel consistencyLevel;

	private final String weaviateObjectClass;

	private final List<MetadataField> filterMetadataFields;

	private final Field[] weaviateSimilaritySearchFields;

	private final WeaviateFilterExpressionConverter filterExpressionConverter;

	private final ObjectMapper objectMapper = new ObjectMapper();

	protected WeaviateVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.weaviateClient, "WeaviateClient must not be null");

		this.weaviateClient = builder.weaviateClient;
		this.consistencyLevel = builder.consistencyLevel;
		this.weaviateObjectClass = builder.weaviateObjectClass;
		this.filterMetadataFields = builder.filterMetadataFields;
		this.filterExpressionConverter = new WeaviateFilterExpressionConverter(
				this.filterMetadataFields.stream().map(MetadataField::name).toList());
		this.weaviateSimilaritySearchFields = buildWeaviateSimilaritySearchFields();
	}

	public static Builder builder(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
		return new Builder(weaviateClient, embeddingModel);
	}

	private Field[] buildWeaviateSimilaritySearchFields() {

		List<Field> searchWeaviateFieldList = new ArrayList<>();

		searchWeaviateFieldList.add(Field.builder().name(CONTENT_FIELD_NAME).build());
		searchWeaviateFieldList.add(Field.builder().name(METADATA_FIELD_NAME).build());
		searchWeaviateFieldList.addAll(this.filterMetadataFields.stream()
			.map(mf -> Field.builder().name(METADATA_FIELD_PREFIX + mf.name()).build())
			.toList());
		searchWeaviateFieldList.add(Field.builder()
			.name(ADDITIONAL_FIELD_NAME)

			.fields(Field.builder().name(ADDITIONAL_ID_FIELD_NAME).build(),
					Field.builder().name(ADDITIONAL_CERTAINTY_FIELD_NAME).build(),
					Field.builder().name(ADDITIONAL_VECTOR_FIELD_NAME).build())
			.build());

		return searchWeaviateFieldList.toArray(new Field[0]);
	}

	@Override
	public void doAdd(List<Document> documents) {

		if (CollectionUtils.isEmpty(documents)) {
			return;
		}

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		List<WeaviateObject> weaviateObjects = documents.stream()
			.map(document -> toWeaviateObject(document, documents, embeddings))
			.toList();

		Result<ObjectGetResponse[]> response = this.weaviateClient.batch()
			.objectsBatcher()
			.withObjects(weaviateObjects.toArray(new WeaviateObject[0]))
			.withConsistencyLevel(this.consistencyLevel.name())
			.run();

		List<String> errorMessages = new ArrayList<>();

		if (response.hasErrors()) {
			errorMessages.add(response.getError()
				.getMessages()
				.stream()
				.map(WeaviateErrorMessage::getMessage)
				.collect(Collectors.joining(System.lineSeparator())));
			throw new RuntimeException("Failed to add documents because: \n" + errorMessages);
		}

		if (response.getResult() != null) {
			for (var r : response.getResult()) {
				if (r.getResult() != null && r.getResult().getErrors() != null) {
					var error = r.getResult().getErrors();
					errorMessages.add(error.getError()
						.stream()
						.map(ObjectsGetResponseAO2Result.ErrorItem::getMessage)
						.collect(Collectors.joining(System.lineSeparator())));
				}
			}
		}

		if (!CollectionUtils.isEmpty(errorMessages)) {
			throw new RuntimeException("Failed to add documents because: \n" + errorMessages);
		}
	}

	private WeaviateObject toWeaviateObject(Document document, List<Document> documents, List<float[]> embeddings) {

		Map<String, Object> fields = new HashMap<>();
		fields.put(CONTENT_FIELD_NAME, document.getText());
		try {
			String metadataString = this.objectMapper.writeValueAsString(document.getMetadata());
			fields.put(METADATA_FIELD_NAME, metadataString);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize the Document metadata: " + document.getText());
		}

		for (MetadataField mf : this.filterMetadataFields) {
			if (document.getMetadata().containsKey(mf.name())) {
				fields.put(METADATA_FIELD_PREFIX + mf.name(), document.getMetadata().get(mf.name()));
			}
		}

		return WeaviateObject.builder()
			.className(this.weaviateObjectClass)
			.id(document.getId())
			.vector(EmbeddingUtils.toFloatArray(embeddings.get(documents.indexOf(document))))
			.properties(fields)
			.build();
	}

	@Override
	public void doDelete(List<String> documentIds) {

		Result<BatchDeleteResponse> result = this.weaviateClient.batch()
			.objectsBatchDeleter()
			.withClassName(this.weaviateObjectClass)
			.withConsistencyLevel(this.consistencyLevel.name())
			.withWhere(WhereFilter.builder()
				.path("id")
				.operator(Operator.ContainsAny)
				.valueString(documentIds.toArray(new String[0]))
				.build())
			.run();

		if (result.hasErrors()) {
			String errorMessages = result.getError()
				.getMessages()
				.stream()
				.map(WeaviateErrorMessage::getMessage)
				.collect(Collectors.joining(","));
			throw new RuntimeException("Failed to delete documents because: \n" + errorMessages);
		}
	}

	@Override
	protected void doDelete(Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try {

			SearchRequest searchRequest = SearchRequest.builder()
				.query("")
				.filterExpression(filterExpression)
				.topK(10000)
				.similarityThresholdAll()
				.build();

			List<Document> matchingDocs = similaritySearch(searchRequest);

			if (!matchingDocs.isEmpty()) {
				List<String> idsToDelete = matchingDocs.stream().map(Document::getId).collect(Collectors.toList());

				delete(idsToDelete);

				logger.debug("Deleted {} documents matching filter expression", idsToDelete.size());
			}
			else {
				logger.debug("No documents found matching filter expression");
			}
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter", e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {

		float[] embedding = this.embeddingModel.embed(request.getQuery());

		GetBuilder.GetBuilderBuilder builder = GetBuilder.builder();

		GetBuilderBuilder queryBuilder = builder.className(this.weaviateObjectClass)
			.withNearVectorFilter(NearVectorArgument.builder()
				.vector(EmbeddingUtils.toFloatArray(embedding))
				.certainty((float) request.getSimilarityThreshold())
				.build())
			.limit(request.getTopK())
			.withWhereFilter(WhereArgument.builder().build())

			.fields(Fields.builder().fields(this.weaviateSimilaritySearchFields).build());

		String graphQLQuery = queryBuilder.build().buildQuery();

		if (request.hasFilterExpression()) {

			String filter = this.filterExpressionConverter.convertExpression(request.getFilterExpression());
			graphQLQuery = graphQLQuery.replace("where:{}", String.format("where:{%s}", filter));
		}
		else {

			graphQLQuery = graphQLQuery.replace("where:{}", "");
		}

		Result<GraphQLResponse> result = this.weaviateClient.graphQL().raw().withQuery(graphQLQuery).run();

		if (result.hasErrors()) {
			throw new IllegalArgumentException(result.getError()
				.getMessages()
				.stream()
				.map(WeaviateErrorMessage::getMessage)
				.collect(Collectors.joining(System.lineSeparator())));
		}

		GraphQLError[] errors = result.getResult().getErrors();
		if (errors != null && errors.length > 0) {
			throw new IllegalArgumentException(Arrays.stream(errors)
				.map(GraphQLError::getMessage)
				.collect(Collectors.joining(System.lineSeparator())));
		}

		@SuppressWarnings("unchecked")
		Optional<Map.Entry<String, Map<?, ?>>> resGetPart = ((Map<String, Map<?, ?>>) result.getResult().getData())
			.entrySet()
			.stream()
			.findFirst();
		if (!resGetPart.isPresent()) {
			return List.of();
		}

		Optional<?> resItemsPart = resGetPart.get().getValue().entrySet().stream().findFirst();
		if (!resItemsPart.isPresent()) {
			return List.of();
		}

		@SuppressWarnings("unchecked")
		List<Map<String, ?>> resItems = ((Map.Entry<String, List<Map<String, ?>>>) resItemsPart.get()).getValue();

		return resItems.stream().map(this::toDocument).toList();
	}

	@SuppressWarnings("unchecked")
	private Document toDocument(Map<String, ?> item) {

		Map<String, ?> additional = (Map<String, ?>) item.get(ADDITIONAL_FIELD_NAME);
		double certainty = (Double) additional.get(ADDITIONAL_CERTAINTY_FIELD_NAME);
		String id = (String) additional.get(ADDITIONAL_ID_FIELD_NAME);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put(DocumentMetadata.DISTANCE.value(), 1 - certainty);

		try {
			String metadataJson = (String) item.get(METADATA_FIELD_NAME);
			if (StringUtils.hasText(metadataJson)) {
				metadata.putAll(this.objectMapper.readValue(metadataJson, Map.class));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		String content = (String) item.get(CONTENT_FIELD_NAME);

		// @formatter:off
		return Document.builder()
			.id(id)
			.text(content)
			.metadata(metadata)
			.score(certainty)
			.build(); // @formatter:on
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.WEAVIATE.value(), operationName)
			.dimensions(this.embeddingModel.dimensions())
			.collectionName(this.weaviateObjectClass);
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.weaviateClient;
		return Optional.of(client);
	}

	public enum ConsistentLevel {

		ONE,

		QUORUM,

		ALL

	}

	public record MetadataField(String name, Type type) {

		public static MetadataField text(String name) {
			Assert.hasText(name, "Text field must not be empty");
			return new MetadataField(name, Type.TEXT);
		}

		public static MetadataField number(String name) {
			Assert.hasText(name, "Number field must not be empty");
			return new MetadataField(name, Type.NUMBER);
		}

		public static MetadataField bool(String name) {
			Assert.hasText(name, "Boolean field name must not be empty");
			return new MetadataField(name, Type.BOOLEAN);
		}

		public enum Type {

			TEXT, NUMBER, BOOLEAN

		}
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private String weaviateObjectClass = "SpringAiWeaviate";

		private ConsistentLevel consistencyLevel = ConsistentLevel.ONE;

		private List<MetadataField> filterMetadataFields = List.of();

		private final WeaviateClient weaviateClient;

		private Builder(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(weaviateClient, "WeaviateClient must not be null");
			this.weaviateClient = weaviateClient;
		}

		public Builder objectClass(String objectClass) {
			Assert.hasText(objectClass, "objectClass must not be empty");
			this.weaviateObjectClass = objectClass;
			return this;
		}

		public Builder consistencyLevel(ConsistentLevel consistencyLevel) {
			Assert.notNull(consistencyLevel, "consistencyLevel must not be null");
			this.consistencyLevel = consistencyLevel;
			return this;
		}

		public Builder filterMetadataFields(List<MetadataField> filterMetadataFields) {
			Assert.notNull(filterMetadataFields, "filterMetadataFields must not be null");
			this.filterMetadataFields = filterMetadataFields;
			return this;
		}

		@Override
		public WeaviateVectorStore build() {
			return new WeaviateVectorStore(this);
		}

	}

}
