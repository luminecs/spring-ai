package org.springframework.ai.vectorstore.typesense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.typesense.api.Client;
import org.typesense.api.FieldTypes;
import org.typesense.model.CollectionResponse;
import org.typesense.model.CollectionSchema;
import org.typesense.model.DeleteDocumentsParameters;
import org.typesense.model.Field;
import org.typesense.model.ImportDocumentsParameters;
import org.typesense.model.IndexAction;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResult;
import org.typesense.model.MultiSearchSearchesParameter;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class TypesenseVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	public static final String DOC_ID_FIELD_NAME = "id";

	public static final String CONTENT_FIELD_NAME = "content";

	public static final String METADATA_FIELD_NAME = "metadata";

	public static final String EMBEDDING_FIELD_NAME = "embedding";

	public static final int OPENAI_EMBEDDING_DIMENSION_SIZE = 1536;

	public static final String DEFAULT_COLLECTION_NAME = "vector_store";

	public static final int INVALID_EMBEDDING_DIMENSION = -1;

	private static final Logger logger = LoggerFactory.getLogger(TypesenseVectorStore.class);

	public final FilterExpressionConverter filterExpressionConverter = new TypesenseFilterExpressionConverter();

	private final Client client;

	private final boolean initializeSchema;

	private final String collectionName;

	private final int embeddingDimension;

	protected TypesenseVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.client, "Typesense must not be null");

		this.client = builder.client;
		this.initializeSchema = builder.initializeSchema;
		this.collectionName = builder.collectionName;
		this.embeddingDimension = builder.embeddingDimension;
	}

	public static Builder builder(Client client, EmbeddingModel embeddingModel) {
		return new Builder(client, embeddingModel);
	}

	@Override
	public void doAdd(List<Document> documents) {
		Assert.notNull(documents, "Documents must not be null");

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		List<HashMap<String, Object>> documentList = documents.stream().map(document -> {
			HashMap<String, Object> typesenseDoc = new HashMap<>();
			typesenseDoc.put(DOC_ID_FIELD_NAME, document.getId());
			typesenseDoc.put(CONTENT_FIELD_NAME, document.getText());
			typesenseDoc.put(METADATA_FIELD_NAME, document.getMetadata());
			typesenseDoc.put(EMBEDDING_FIELD_NAME, embeddings.get(documents.indexOf(document)));

			return typesenseDoc;
		}).toList();

		ImportDocumentsParameters importDocumentsParameters = new ImportDocumentsParameters();
		importDocumentsParameters.action(IndexAction.UPSERT);

		try {
			this.client.collections(this.collectionName).documents().import_(documentList, importDocumentsParameters);

			logger.info("Added {} documents", documentList.size());
		}
		catch (Exception e) {
			logger.error("Failed to add documents", e);
		}
	}

	@Override
	public void doDelete(List<String> idList) {
		DeleteDocumentsParameters deleteDocumentsParameters = new DeleteDocumentsParameters();
		deleteDocumentsParameters.filterBy(DOC_ID_FIELD_NAME + ":=[" + String.join(",", idList) + "]");

		try {
			int deletedDocs = (Integer) this.client.collections(this.collectionName)
				.documents()
				.delete(deleteDocumentsParameters)
				.getOrDefault("num_deleted", 0);

			if (deletedDocs < idList.size()) {
				logger.warn("Failed to delete all documents");
			}
		}
		catch (Exception e) {
			logger.error("Failed to delete documents", e);
		}
	}

	@Override
	protected void doDelete(Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try {
			String filterStr = this.filterExpressionConverter.convertExpression(filterExpression);
			DeleteDocumentsParameters deleteDocumentsParameters = new DeleteDocumentsParameters();
			deleteDocumentsParameters.filterBy(filterStr);

			Map<String, Object> response = this.client.collections(this.collectionName)
				.documents()
				.delete(deleteDocumentsParameters);

			int deletedDocs = (Integer) response.getOrDefault("num_deleted", 0);
			if (deletedDocs == 0) {
				logger.warn("No documents were deleted matching filter expression");
			}
			else {
				logger.debug("Deleted {} documents matching filter expression", deletedDocs);
			}
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter", e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		Assert.notNull(request.getQuery(), "Query string must not be null");

		String nativeFilterExpressions = (request.getFilterExpression() != null)
				? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";

		logger.info("Filter expression: {}", nativeFilterExpressions);

		float[] embedding = this.embeddingModel.embed(request.getQuery());

		MultiSearchCollectionParameters multiSearchCollectionParameters = new MultiSearchCollectionParameters();
		multiSearchCollectionParameters.collection(this.collectionName);
		multiSearchCollectionParameters.q("*");

		Stream<Float> floatStream = IntStream.range(0, embedding.length).mapToObj(i -> embedding[i]);

		String vectorQuery = EMBEDDING_FIELD_NAME + ":(" + "["
				+ String.join(",", floatStream.map(String::valueOf).toList()) + "], " + "k: " + request.getTopK() + ", "
				+ "distance_threshold: " + (1 - request.getSimilarityThreshold()) + ")";

		multiSearchCollectionParameters.vectorQuery(vectorQuery);
		multiSearchCollectionParameters.filterBy(nativeFilterExpressions);

		MultiSearchSearchesParameter multiSearchesParameter = new MultiSearchSearchesParameter()
			.addSearchesItem(multiSearchCollectionParameters);

		try {
			MultiSearchResult result = this.client.multiSearch.perform(multiSearchesParameter,
					Map.of("query_by", EMBEDDING_FIELD_NAME));

			List<Document> documents = result.getResults()
				.stream()
				.flatMap(searchResult -> searchResult.getHits().stream().map(hit -> {
					Map<String, Object> rawDocument = hit.getDocument();
					String docId = rawDocument.get(DOC_ID_FIELD_NAME).toString();
					String content = rawDocument.get(CONTENT_FIELD_NAME).toString();
					Map<String, Object> metadata = rawDocument.get(METADATA_FIELD_NAME) instanceof Map
							? (Map<String, Object>) rawDocument.get(METADATA_FIELD_NAME) : Map.of();
					metadata.put(DocumentMetadata.DISTANCE.value(), hit.getVectorDistance());
					return Document.builder()
						.id(docId)
						.text(content)
						.metadata(metadata)
						.score(1.0 - hit.getVectorDistance())
						.build();
				}))
				.toList();

			logger.info("Found {} documents", documents.size());
			return documents;
		}
		catch (Exception e) {
			logger.error("Failed to search documents", e);
			return List.of();
		}
	}

	int embeddingDimensions() {
		if (this.embeddingDimension != INVALID_EMBEDDING_DIMENSION) {
			return this.embeddingDimension;
		}
		try {
			int embeddingDimensions = this.embeddingModel.dimensions();
			if (embeddingDimensions > 0) {
				return embeddingDimensions;
			}
		}
		catch (Exception e) {
			logger.warn("Failed to obtain the embedding dimensions from the embedding model and fall backs to default:"
					+ this.embeddingDimension, e);
		}
		return OPENAI_EMBEDDING_DIMENSION_SIZE;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.initializeSchema) {
			this.createCollection();
		}
	}

	private boolean hasCollection() {
		try {
			this.client.collections(this.collectionName).retrieve();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	void createCollection() {
		if (this.hasCollection()) {
			logger.info("Collection {} already exists", this.collectionName);
			return;
		}

		CollectionSchema collectionSchema = new CollectionSchema();

		collectionSchema.name(this.collectionName)
			.addFieldsItem(new Field().name(DOC_ID_FIELD_NAME).type(FieldTypes.STRING).optional(false))
			.addFieldsItem(new Field().name(CONTENT_FIELD_NAME).type(FieldTypes.STRING).optional(false))
			.addFieldsItem(new Field().name(METADATA_FIELD_NAME).type(FieldTypes.OBJECT).optional(true))
			.addFieldsItem(new Field().name(EMBEDDING_FIELD_NAME)
				.type(FieldTypes.FLOAT_ARRAY)
				.numDim(this.embeddingDimensions())
				.optional(false))
			.enableNestedFields(true);

		try {
			this.client.collections().create(collectionSchema);
			logger.info("Collection {} created", this.collectionName);
		}
		catch (Exception e) {
			logger.error("Failed to create collection {}", this.collectionName, e);
		}
	}

	void dropCollection() {
		if (!this.hasCollection()) {
			logger.info("Collection {} does not exist", this.collectionName);
			return;
		}

		try {
			this.client.collections(this.collectionName).delete();
			logger.info("Collection {} dropped", this.collectionName);
		}
		catch (Exception e) {
			logger.error("Failed to drop collection {}", this.collectionName, e);
		}
	}

	@Nullable
	Map<String, Object> getCollectionInfo() {
		try {
			CollectionResponse retrievedCollection = this.client.collections(this.collectionName).retrieve();
			return Map.of("name", retrievedCollection.getName(), "num_documents",
					retrievedCollection.getNumDocuments());
		}
		catch (Exception e) {
			logger.error("Failed to retrieve collection info", e);
			return null;
		}

	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.TYPESENSE.value(), operationName)
			.dimensions(this.embeddingModel.dimensions())
			.collectionName(this.collectionName)
			.fieldName(EMBEDDING_FIELD_NAME)
			.similarityMetric(VectorStoreSimilarityMetric.COSINE.value());
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.client;
		return Optional.of(client);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private String collectionName = DEFAULT_COLLECTION_NAME;

		private int embeddingDimension = INVALID_EMBEDDING_DIMENSION;

		private final Client client;

		private boolean initializeSchema = false;

		public Builder(Client client, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(client, "client must not be null");
			this.client = client;
		}

		public Builder collectionName(String collectionName) {
			Assert.hasText(collectionName, "collectionName must not be empty");
			this.collectionName = collectionName;
			return this;
		}

		public Builder embeddingDimension(int embeddingDimension) {
			Assert.isTrue(embeddingDimension > 0, "Embedding dimension must be greater than 0");
			this.embeddingDimension = embeddingDimension;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		@Override
		public TypesenseVectorStore build() {
			return new TypesenseVectorStore(this);
		}

	}

}
