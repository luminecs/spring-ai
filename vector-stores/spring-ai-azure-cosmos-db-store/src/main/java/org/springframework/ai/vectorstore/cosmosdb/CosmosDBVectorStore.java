package org.springframework.ai.vectorstore.cosmosdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class CosmosDBVectorStore extends AbstractObservationVectorStore implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(CosmosDBVectorStore.class);

	private final CosmosAsyncClient cosmosClient;

	private final String containerName;

	private final String databaseName;

	private final String partitionKeyPath;

	private final int vectorStoreThroughput;

	private final long vectorDimensions;

	private final List<String> metadataFieldsList;

	private CosmosAsyncContainer container;

	protected CosmosDBVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.cosmosClient, "CosmosClient must not be null");
		Assert.hasText(builder.containerName, "Container name must not be empty");
		Assert.hasText(builder.databaseName, "Database name must not be empty");

		this.cosmosClient = builder.cosmosClient;
		this.containerName = builder.containerName;
		this.databaseName = builder.databaseName;
		this.partitionKeyPath = builder.partitionKeyPath;
		this.vectorStoreThroughput = builder.vectorStoreThroughput;
		this.vectorDimensions = builder.vectorDimensions;
		this.metadataFieldsList = builder.metadataFieldsList;

		this.cosmosClient.createDatabaseIfNotExists(this.databaseName).block();
		initializeContainer(this.containerName, this.databaseName, this.vectorStoreThroughput, this.vectorDimensions,
				this.partitionKeyPath);
	}

	public static Builder builder(CosmosAsyncClient cosmosClient, EmbeddingModel embeddingModel) {
		return new Builder(cosmosClient, embeddingModel);
	}

	private void initializeContainer(String containerName, String databaseName, int vectorStoreThroughput,
			long vectorDimensions, String partitionKeyPath) {

		if (this.vectorStoreThroughput == 0) {
			vectorStoreThroughput = 400;
		}
		if (this.partitionKeyPath == null) {
			partitionKeyPath = "/id";
		}

		PartitionKeyDefinition subPartitionKeyDefinition = new PartitionKeyDefinition();
		List<String> pathsFromCommaSeparatedList = new ArrayList<String>();
		String[] subPartitionKeyPaths = partitionKeyPath.split(",");
		Collections.addAll(pathsFromCommaSeparatedList, subPartitionKeyPaths);
		if (subPartitionKeyPaths.length > 1) {
			subPartitionKeyDefinition.setPaths(pathsFromCommaSeparatedList);
			subPartitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
		}
		else {
			subPartitionKeyDefinition.setPaths(Collections.singletonList(this.partitionKeyPath));
			subPartitionKeyDefinition.setKind(PartitionKind.HASH);
		}
		CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(this.containerName,
				subPartitionKeyDefinition);

		CosmosVectorEmbeddingPolicy embeddingPolicy = new CosmosVectorEmbeddingPolicy();
		CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
		embedding.setPath("/embedding");
		embedding.setDataType(CosmosVectorDataType.FLOAT32);
		embedding.setDimensions(this.vectorDimensions);
		embedding.setDistanceFunction(CosmosVectorDistanceFunction.COSINE);
		embeddingPolicy.setCosmosVectorEmbeddings(Collections.singletonList(embedding));
		collectionDefinition.setVectorEmbeddingPolicy(embeddingPolicy);

		IndexingPolicy indexingPolicy = new IndexingPolicy();
		indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
		ExcludedPath excludedPath = new ExcludedPath("/*");
		indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));
		IncludedPath includedPath1 = new IncludedPath("/metadata/?");
		IncludedPath includedPath2 = new IncludedPath("/content/?");
		indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));
		CosmosVectorIndexSpec cosmosVectorIndexSpec = new CosmosVectorIndexSpec();
		cosmosVectorIndexSpec.setPath("/embedding");
		cosmosVectorIndexSpec.setType(CosmosVectorIndexType.DISK_ANN.toString());
		indexingPolicy.setVectorIndexes(List.of(cosmosVectorIndexSpec));
		collectionDefinition.setIndexingPolicy(indexingPolicy);

		ThroughputProperties throughputProperties = ThroughputProperties
			.createManualThroughput(this.vectorStoreThroughput);
		CosmosAsyncDatabase cosmosAsyncDatabase = this.cosmosClient.getDatabase(this.databaseName);
		cosmosAsyncDatabase.createContainerIfNotExists(collectionDefinition, throughputProperties).block();
		this.container = cosmosAsyncDatabase.getContainer(this.containerName);
	}

	@Override
	public void close() {
		if (this.cosmosClient != null) {
			this.cosmosClient.close();
			logger.info("Cosmos DB client closed successfully.");
		}
	}

	private JsonNode mapCosmosDocument(Document document, float[] queryEmbedding) {
		ObjectMapper objectMapper = new ObjectMapper();

		String id = document.getId();
		String content = document.getText();

		JsonNode metadataNode = objectMapper.valueToTree(document.getMetadata());
		JsonNode embeddingNode = objectMapper.valueToTree(queryEmbedding);

		ObjectNode objectNode = objectMapper.createObjectNode();

		objectNode.put("id", id);
		objectNode.put("content", content);
		objectNode.set("metadata", metadataNode);
		objectNode.set("embedding", embeddingNode);

		return objectNode;
	}

	@Override
	public void doAdd(List<Document> documents) {

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		List<ImmutablePair<String, CosmosItemOperation>> itemOperationsWithIds = documents.stream().map(doc -> {
			CosmosItemOperation operation = CosmosBulkOperations.getCreateItemOperation(
					mapCosmosDocument(doc, embeddings.get(documents.indexOf(doc))), new PartitionKey(doc.getId()));
			return new ImmutablePair<>(doc.getId(), operation);

		}).toList();

		try {

			List<CosmosItemOperation> itemOperations = itemOperationsWithIds.stream()
				.map(ImmutablePair::getValue)
				.collect(Collectors.toList());

			this.container.executeBulkOperations(Flux.fromIterable(itemOperations)).doOnNext(response -> {
				if (response != null && response.getResponse() != null) {
					int statusCode = response.getResponse().getStatusCode();
					if (statusCode == 409) {

						String documentId = itemOperationsWithIds.stream()
							.filter(pair -> pair.getValue().equals(response.getOperation()))
							.findFirst()
							.map(ImmutablePair::getKey)
							.orElse("Unknown ID");

						String errorMessage = String.format("Duplicate document id: %s", documentId);
						logger.error(errorMessage);
						throw new RuntimeException(errorMessage);

					}
					else {
						logger.info("Document added with status: {}", statusCode);
					}
				}
				else {
					logger.warn("Received a null response or null status code for a document operation.");
				}
			})
				.doOnError(error -> logger.error("Error adding document: {}", error.getMessage()))
				.doOnComplete(() -> logger.info("Bulk operation completed successfully."))
				.blockLast();
		}
		catch (Exception e) {
			logger.error("Exception occurred during bulk add operation: {}", e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void doDelete(List<String> idList) {
		try {

			List<CosmosItemOperation> itemOperations = idList.stream()
				.map(id -> CosmosBulkOperations.getDeleteItemOperation(id, new PartitionKey(id)))
				.collect(Collectors.toList());

			this.container.executeBulkOperations(Flux.fromIterable(itemOperations))
				.doOnNext(response -> logger.info("Document deleted with status: {}",
						response.getResponse().getStatusCode()))
				.doOnError(error -> logger.error("Error deleting document: {}", error.getMessage()))
				.blockLast();
		}
		catch (Exception e) {
			logger.error("Exception while deleting documents: {}", e.getMessage());
		}
	}

	@Override
	public List<Document> similaritySearch(String query) {
		return similaritySearch(SearchRequest.builder().query(query).build());
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {

		if (request.getTopK() > 1000) {
			throw new IllegalArgumentException("Top K must be 1000 or less.");
		}

		float[] embedding = this.embeddingModel.embed(request.getQuery());

		logger.info("similarity threshold: {}", request.getSimilarityThreshold());

		List<Float> embeddingList = IntStream.range(0, embedding.length)
			.mapToObj(i -> embedding[i])
			.collect(Collectors.toList());

		StringBuilder queryBuilder = new StringBuilder("SELECT TOP @topK * FROM c WHERE ");
		queryBuilder.append("VectorDistance(c.embedding, @embedding) > @similarityThreshold");

		Filter.Expression filterExpression = request.getFilterExpression();
		if (filterExpression != null) {
			CosmosDBFilterExpressionConverter filterExpressionConverter = new CosmosDBFilterExpressionConverter(
					this.metadataFieldsList);

			String filterQuery = filterExpressionConverter.convertExpression(filterExpression);
			queryBuilder.append(" AND ").append(filterQuery);
		}

		queryBuilder.append(" ORDER BY VectorDistance(c.embedding, @embedding)");

		String query = queryBuilder.toString();
		List<SqlParameter> parameters = new ArrayList<>();
		parameters.add(new SqlParameter("@embedding", embeddingList));
		parameters.add(new SqlParameter("@topK", request.getTopK()));
		parameters.add(new SqlParameter("@similarityThreshold", request.getSimilarityThreshold()));

		SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, parameters);
		CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

		CosmosPagedFlux<JsonNode> pagedFlux = this.container.queryItems(sqlQuerySpec, options, JsonNode.class);

		logger.info("Executing similarity search query: {}", query);
		try {

			List<JsonNode> documents = pagedFlux.byPage()
				.flatMap(page -> Flux.fromIterable(page.getResults()))
				.collectList()
				.block();

			List<Document> docs = documents.stream()
				.map(doc -> Document.builder().id(doc.get("id").asText()).text(doc.get("content").asText()).build())
				.collect(Collectors.toList());

			return docs != null ? docs : List.of();
		}
		catch (Exception e) {
			logger.error("Error during similarity search: {}", e.getMessage());
			return List.of();
		}
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
		return VectorStoreObservationContext.builder(VectorStoreProvider.COSMOSDB.value(), operationName)
			.collectionName(this.container.getId())
			.dimensions(this.embeddingModel.dimensions())
			.namespace(this.container.getDatabase().getId())
			.similarityMetric("cosine");
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.container;
		return Optional.of(client);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final CosmosAsyncClient cosmosClient;

		@Nullable
		private String containerName;

		@Nullable
		private String databaseName;

		@Nullable
		private String partitionKeyPath;

		private int vectorStoreThroughput = 400;

		private long vectorDimensions = 1536;

		private List<String> metadataFieldsList = new ArrayList<>();

		private Builder(CosmosAsyncClient cosmosClient, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(cosmosClient, "CosmosClient must not be null");
			this.cosmosClient = cosmosClient;
		}

		public Builder containerName(String containerName) {
			Assert.hasText(containerName, "Container name must not be empty");
			this.containerName = containerName;
			return this;
		}

		public Builder databaseName(String databaseName) {
			Assert.hasText(databaseName, "Database name must not be empty");
			this.databaseName = databaseName;
			return this;
		}

		public Builder partitionKeyPath(String partitionKeyPath) {
			Assert.hasText(partitionKeyPath, "Partition key path must not be empty");
			this.partitionKeyPath = partitionKeyPath;
			return this;
		}

		public Builder vectorStoreThroughput(int vectorStoreThroughput) {
			Assert.isTrue(vectorStoreThroughput > 0, "Vector store throughput must be positive");
			this.vectorStoreThroughput = vectorStoreThroughput;
			return this;
		}

		public Builder vectorDimensions(long vectorDimensions) {
			Assert.isTrue(vectorDimensions > 0, "Vector dimensions must be positive");
			this.vectorDimensions = vectorDimensions;
			return this;
		}

		public Builder metadataFields(List<String> metadataFieldsList) {
			this.metadataFieldsList = metadataFieldsList != null ? new ArrayList<>(this.metadataFieldsList)
					: new ArrayList<>();
			return this;
		}

		@Override
		public CosmosDBVectorStore build() {
			return new CosmosDBVectorStore(this);
		}

	}

}
