package org.springframework.ai.vectorstore.milvus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.exception.ParamException;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.R.Status;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.response.SearchResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.model.EmbeddingUtils;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class MilvusVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	public static final int OPENAI_EMBEDDING_DIMENSION_SIZE = 1536;

	public static final int INVALID_EMBEDDING_DIMENSION = -1;

	public static final String DEFAULT_DATABASE_NAME = "default";

	public static final String DEFAULT_COLLECTION_NAME = "vector_store";

	public static final String DOC_ID_FIELD_NAME = "doc_id";

	public static final String CONTENT_FIELD_NAME = "content";

	public static final String METADATA_FIELD_NAME = "metadata";

	public static final String EMBEDDING_FIELD_NAME = "embedding";

	public static final String SIMILARITY_FIELD_NAME = "score";

	private static final Logger logger = LoggerFactory.getLogger(MilvusVectorStore.class);

	private static final Map<MetricType, VectorStoreSimilarityMetric> SIMILARITY_TYPE_MAPPING = Map.of(
			MetricType.COSINE, VectorStoreSimilarityMetric.COSINE, MetricType.L2, VectorStoreSimilarityMetric.EUCLIDEAN,
			MetricType.IP, VectorStoreSimilarityMetric.DOT);

	public final FilterExpressionConverter filterExpressionConverter = new MilvusFilterExpressionConverter();

	private final MilvusServiceClient milvusClient;

	private final boolean initializeSchema;

	private final String databaseName;

	private final String collectionName;

	private final int embeddingDimension;

	private final IndexType indexType;

	private final MetricType metricType;

	private final String indexParameters;

	private final String idFieldName;

	private final boolean isAutoId;

	private final String contentFieldName;

	private final String metadataFieldName;

	private final String embeddingFieldName;

	protected MilvusVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.milvusClient, "milvusClient must not be null");

		this.milvusClient = builder.milvusClient;
		this.initializeSchema = builder.initializeSchema;
		this.databaseName = builder.databaseName;
		this.collectionName = builder.collectionName;
		this.embeddingDimension = builder.embeddingDimension;
		this.indexType = builder.indexType;
		this.metricType = builder.metricType;
		this.indexParameters = builder.indexParameters;
		this.idFieldName = builder.idFieldName;
		this.isAutoId = builder.isAutoId;
		this.contentFieldName = builder.contentFieldName;
		this.metadataFieldName = builder.metadataFieldName;
		this.embeddingFieldName = builder.embeddingFieldName;
	}

	public static Builder builder(MilvusServiceClient milvusServiceClient, EmbeddingModel embeddingModel) {
		return new Builder(milvusServiceClient, embeddingModel);
	}

	@Override
	public void doAdd(List<Document> documents) {

		Assert.notNull(documents, "Documents must not be null");

		List<String> docIdArray = new ArrayList<>();
		List<String> contentArray = new ArrayList<>();
		List<JsonObject> metadataArray = new ArrayList<>();
		List<List<Float>> embeddingArray = new ArrayList<>();

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		for (Document document : documents) {
			docIdArray.add(document.getId());

			contentArray.add(document.getText());
			Gson gson = new Gson();
			String jsonString = gson.toJson(document.getMetadata());
			metadataArray.add(gson.fromJson(jsonString, JsonObject.class));
			embeddingArray.add(EmbeddingUtils.toList(embeddings.get(documents.indexOf(document))));
		}

		List<InsertParam.Field> fields = new ArrayList<>();

		if (!this.isAutoId) {
			fields.add(new InsertParam.Field(this.idFieldName, docIdArray));
		}
		fields.add(new InsertParam.Field(this.contentFieldName, contentArray));
		fields.add(new InsertParam.Field(this.metadataFieldName, metadataArray));
		fields.add(new InsertParam.Field(this.embeddingFieldName, embeddingArray));

		InsertParam insertParam = InsertParam.newBuilder()
			.withDatabaseName(this.databaseName)
			.withCollectionName(this.collectionName)
			.withFields(fields)
			.build();

		R<MutationResult> status = this.milvusClient.insert(insertParam);
		if (status.getException() != null) {
			throw new RuntimeException("Failed to insert:", status.getException());
		}
	}

	@Override
	public void doDelete(List<String> idList) {
		Assert.notNull(idList, "Document id list must not be null");

		String deleteExpression = String.format("%s in [%s]", this.idFieldName,
				idList.stream().map(id -> "'" + id + "'").collect(Collectors.joining(",")));

		R<MutationResult> status = this.milvusClient.delete(DeleteParam.newBuilder()
			.withDatabaseName(this.databaseName)
			.withCollectionName(this.collectionName)
			.withExpr(deleteExpression)
			.build());

		long deleteCount = status.getData().getDeleteCnt();
		if (deleteCount != idList.size()) {
			logger.warn(String.format("Deleted only %s entries from requested %s ", deleteCount, idList.size()));
		}
	}

	@Override
	protected void doDelete(Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try {
			String nativeFilterExpression = this.filterExpressionConverter.convertExpression(filterExpression);

			R<MutationResult> status = this.milvusClient.delete(DeleteParam.newBuilder()
				.withDatabaseName(this.databaseName)
				.withCollectionName(this.collectionName)
				.withExpr(nativeFilterExpression)
				.build());

			if (status.getStatus() != Status.Success.getCode()) {
				throw new IllegalStateException("Failed to delete documents by filter: " + status.getMessage());
			}

			long deleteCount = status.getData().getDeleteCnt();
			logger.debug("Deleted {} documents matching filter expression", deleteCount);
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		String nativeFilterExpressions = "";
		String searchParamsJson = null;
		if (request instanceof MilvusSearchRequest milvusReq) {
			nativeFilterExpressions = StringUtils.hasText(milvusReq.getNativeExpression())
					? milvusReq.getNativeExpression() : getConvertedFilterExpression(request);

			searchParamsJson = StringUtils.hasText(milvusReq.getSearchParamsJson()) ? milvusReq.getSearchParamsJson()
					: null;
		}
		else {
			nativeFilterExpressions = getConvertedFilterExpression(request);
		}

		Assert.notNull(request.getQuery(), "Query string must not be null");
		List<String> outFieldNames = new ArrayList<>();
		outFieldNames.add(this.idFieldName);
		outFieldNames.add(this.contentFieldName);
		outFieldNames.add(this.metadataFieldName);
		float[] embedding = this.embeddingModel.embed(request.getQuery());

		var searchParamBuilder = SearchParam.newBuilder()
			.withDatabaseName(this.databaseName)
			.withCollectionName(this.collectionName)
			.withConsistencyLevel(ConsistencyLevelEnum.STRONG)
			.withMetricType(this.metricType)
			.withOutFields(outFieldNames)
			.withTopK(request.getTopK())
			.withVectors(List.of(EmbeddingUtils.toList(embedding)))
			.withVectorFieldName(this.embeddingFieldName);

		if (StringUtils.hasText(nativeFilterExpressions)) {
			searchParamBuilder.withExpr(nativeFilterExpressions);
		}

		if (StringUtils.hasText(searchParamsJson)) {
			searchParamBuilder.withParams(searchParamsJson);
		}

		R<SearchResults> respSearch = this.milvusClient.search(searchParamBuilder.build());

		if (respSearch.getException() != null) {
			throw new RuntimeException("Search failed!", respSearch.getException());
		}

		SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());

		return wrapperSearch.getRowRecords(0)
			.stream()
			.filter(rowRecord -> getResultSimilarity(rowRecord) >= request.getSimilarityThreshold())
			.map(rowRecord -> {
				String docId = String.valueOf(rowRecord.get(this.idFieldName));
				String content = (String) rowRecord.get(this.contentFieldName);
				JsonObject metadata = new JsonObject();
				try {
					metadata = (JsonObject) rowRecord.get(this.metadataFieldName);

					metadata.addProperty(DocumentMetadata.DISTANCE.value(), 1 - getResultSimilarity(rowRecord));
				}
				catch (ParamException e) {

				}
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				return Document.builder()
					.id(docId)
					.text(content)
					.metadata((metadata != null) ? gson.fromJson(metadata, type) : Map.of())
					.score((double) getResultSimilarity(rowRecord))
					.build();
			})
			.toList();
	}

	private String getConvertedFilterExpression(SearchRequest request) {
		return (request.getFilterExpression() != null)
				? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";
	}

	private float getResultSimilarity(RowRecord rowRecord) {
		Float score = (Float) rowRecord.get(SIMILARITY_FIELD_NAME);
		return (this.metricType == MetricType.IP || this.metricType == MetricType.COSINE) ? score : (1 - score);
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!this.initializeSchema) {
			return;
		}

		this.createCollection();
	}

	void releaseCollection() {
		if (isDatabaseCollectionExists()) {
			this.milvusClient
				.releaseCollection(ReleaseCollectionParam.newBuilder().withCollectionName(this.collectionName).build());
		}
	}

	private boolean isDatabaseCollectionExists() {
		return this.milvusClient
			.hasCollection(HasCollectionParam.newBuilder()
				.withDatabaseName(this.databaseName)
				.withCollectionName(this.collectionName)
				.build())
			.getData();
	}

	void createCollection() {

		if (!isDatabaseCollectionExists()) {
			createCollection(this.databaseName, this.collectionName, this.idFieldName, this.isAutoId,
					this.contentFieldName, this.metadataFieldName, this.embeddingFieldName);
		}

		R<DescribeIndexResponse> indexDescriptionResponse = this.milvusClient
			.describeIndex(DescribeIndexParam.newBuilder()
				.withDatabaseName(this.databaseName)
				.withCollectionName(this.collectionName)
				.build());

		if (indexDescriptionResponse.getData() == null) {
			R<RpcStatus> indexStatus = this.milvusClient.createIndex(CreateIndexParam.newBuilder()
				.withDatabaseName(this.databaseName)
				.withCollectionName(this.collectionName)
				.withFieldName(this.embeddingFieldName)
				.withIndexType(this.indexType)
				.withMetricType(this.metricType)
				.withExtraParam(this.indexParameters)
				.withSyncMode(Boolean.FALSE)
				.build());

			if (indexStatus.getException() != null) {
				throw new RuntimeException("Failed to create Index", indexStatus.getException());
			}
		}

		R<RpcStatus> loadCollectionStatus = this.milvusClient.loadCollection(LoadCollectionParam.newBuilder()
			.withDatabaseName(this.databaseName)
			.withCollectionName(this.collectionName)
			.build());

		if (loadCollectionStatus.getException() != null) {
			throw new RuntimeException("Collection loading failed!", loadCollectionStatus.getException());
		}
	}

	void createCollection(String databaseName, String collectionName, String idFieldName, boolean isAutoId,
			String contentFieldName, String metadataFieldName, String embeddingFieldName) {
		FieldType docIdFieldType = FieldType.newBuilder()
			.withName(idFieldName)
			.withDataType(DataType.VarChar)
			.withMaxLength(36)
			.withPrimaryKey(true)
			.withAutoID(isAutoId)
			.build();
		FieldType contentFieldType = FieldType.newBuilder()
			.withName(contentFieldName)
			.withDataType(DataType.VarChar)
			.withMaxLength(65535)
			.build();
		FieldType metadataFieldType = FieldType.newBuilder()
			.withName(metadataFieldName)
			.withDataType(DataType.JSON)
			.build();
		FieldType embeddingFieldType = FieldType.newBuilder()
			.withName(embeddingFieldName)
			.withDataType(DataType.FloatVector)
			.withDimension(this.embeddingDimensions())
			.build();

		CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
			.withDatabaseName(databaseName)
			.withCollectionName(collectionName)
			.withDescription("Spring AI Vector Store")
			.withConsistencyLevel(ConsistencyLevelEnum.STRONG)
			.withShardsNum(2)
			.addFieldType(docIdFieldType)
			.addFieldType(contentFieldType)
			.addFieldType(metadataFieldType)
			.addFieldType(embeddingFieldType)
			.build();

		R<RpcStatus> collectionStatus = this.milvusClient.createCollection(createCollectionReq);
		if (collectionStatus.getException() != null) {
			throw new RuntimeException("Failed to create collection", collectionStatus.getException());
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

	void dropCollection() {

		R<RpcStatus> status = this.milvusClient
			.releaseCollection(ReleaseCollectionParam.newBuilder().withCollectionName(this.collectionName).build());

		if (status.getException() != null) {
			throw new RuntimeException("Release collection failed!", status.getException());
		}

		status = this.milvusClient
			.dropIndex(DropIndexParam.newBuilder().withCollectionName(this.collectionName).build());

		if (status.getException() != null) {
			throw new RuntimeException("Drop Index failed!", status.getException());
		}

		status = this.milvusClient.dropCollection(DropCollectionParam.newBuilder()
			.withDatabaseName(this.databaseName)
			.withCollectionName(this.collectionName)
			.build());

		if (status.getException() != null) {
			throw new RuntimeException("Drop Collection failed!", status.getException());
		}
	}

	@Override
	public org.springframework.ai.vectorstore.observation.VectorStoreObservationContext.Builder createObservationContextBuilder(
			String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.MILVUS.value(), operationName)
			.collectionName(this.collectionName)
			.dimensions(this.embeddingModel.dimensions())
			.similarityMetric(getSimilarityMetric())
			.namespace(this.databaseName);
	}

	private String getSimilarityMetric() {
		if (!SIMILARITY_TYPE_MAPPING.containsKey(this.metricType)) {
			return this.metricType.name();
		}
		return SIMILARITY_TYPE_MAPPING.get(this.metricType).value();
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.milvusClient;
		return Optional.of(client);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final MilvusServiceClient milvusClient;

		private String databaseName = DEFAULT_DATABASE_NAME;

		private String collectionName = DEFAULT_COLLECTION_NAME;

		private int embeddingDimension = INVALID_EMBEDDING_DIMENSION;

		private IndexType indexType = IndexType.IVF_FLAT;

		private MetricType metricType = MetricType.COSINE;

		private String indexParameters = "{\"nlist\":1024}";

		private String idFieldName = DOC_ID_FIELD_NAME;

		private boolean isAutoId = false;

		private String contentFieldName = CONTENT_FIELD_NAME;

		private String metadataFieldName = METADATA_FIELD_NAME;

		private String embeddingFieldName = EMBEDDING_FIELD_NAME;

		private boolean initializeSchema = false;

		private Builder(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(milvusClient, "milvusClient must not be null");
			this.milvusClient = milvusClient;
		}

		public Builder metricType(MetricType metricType) {
			Assert.notNull(metricType, "Collection Name must not be empty");
			Assert.isTrue(metricType == MetricType.IP || metricType == MetricType.L2 || metricType == MetricType.COSINE,
					"Only the text metric types IP and L2 are supported");
			this.metricType = metricType;
			return this;
		}

		public Builder indexType(IndexType indexType) {
			this.indexType = indexType;
			return this;
		}

		public Builder indexParameters(String indexParameters) {
			this.indexParameters = indexParameters;
			return this;
		}

		public Builder databaseName(String databaseName) {
			this.databaseName = databaseName;
			return this;
		}

		public Builder collectionName(String collectionName) {
			this.collectionName = collectionName;
			return this;
		}

		public Builder embeddingDimension(int newEmbeddingDimension) {
			Assert.isTrue(newEmbeddingDimension >= 1 && newEmbeddingDimension <= 32768,
					"Dimension has to be withing the boundaries 1 and 32768 (inclusively)");
			this.embeddingDimension = newEmbeddingDimension;
			return this;
		}

		public Builder iDFieldName(String idFieldName) {
			this.idFieldName = idFieldName;
			return this;
		}

		public Builder autoId(boolean isAutoId) {
			this.isAutoId = isAutoId;
			return this;
		}

		public Builder contentFieldName(String contentFieldName) {
			this.contentFieldName = contentFieldName;
			return this;
		}

		public Builder metadataFieldName(String metadataFieldName) {
			this.metadataFieldName = metadataFieldName;
			return this;
		}

		public Builder embeddingFieldName(String embeddingFieldName) {
			this.embeddingFieldName = embeddingFieldName;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		public MilvusVectorStore build() {
			return new MilvusVectorStore(this);
		}

	}

}
