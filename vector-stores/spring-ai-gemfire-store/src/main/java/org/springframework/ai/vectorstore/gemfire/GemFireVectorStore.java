package org.springframework.ai.vectorstore.gemfire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

public class GemFireVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GemFireVectorStore.class);

	private static final String DEFAULT_URI = "http{ssl}://{host}:{port}/gemfire-vectordb/v1/indexes";

	private static final String EMBEDDINGS = "/embeddings";

	private static final String QUERY = "/query";

	private static final String DOCUMENT_FIELD = "document";

	public static final String DEFAULT_HOST = "localhost";

	public static final int DEFAULT_PORT = 8080;

	public static final String DEFAULT_INDEX_NAME = "spring-ai-gemfire-index";

	public static final int UPPER_BOUND_BEAM_WIDTH = 3200;

	public static final int DEFAULT_BEAM_WIDTH = 100;

	private static final int UPPER_BOUND_MAX_CONNECTIONS = 512;

	public static final int DEFAULT_MAX_CONNECTIONS = 16;

	public static final String DEFAULT_SIMILARITY_FUNCTION = "COSINE";

	public static final String[] DEFAULT_FIELDS = new String[] {};

	public static final int DEFAULT_BUCKETS = 0;

	public static final boolean DEFAULT_SSL_ENABLED = false;

	private final WebClient client;

	private final boolean initializeSchema;

	private final ObjectMapper objectMapper;

	private final String indexName;

	private final int beamWidth;

	private final int maxConnections;

	private final int buckets;

	private final String vectorSimilarityFunction;

	private final String[] fields;

	protected GemFireVectorStore(Builder builder) {
		super(builder);

		this.initializeSchema = builder.initializeSchema;
		this.indexName = builder.indexName;
		this.beamWidth = builder.beamWidth;
		this.maxConnections = builder.maxConnections;
		this.buckets = builder.buckets;
		this.vectorSimilarityFunction = builder.vectorSimilarityFunction;
		this.fields = builder.fields;

		String base = UriComponentsBuilder.fromUriString(DEFAULT_URI)
			.build(builder.sslEnabled ? "s" : "", builder.host, builder.port)
			.toString();
		this.client = WebClient.create(base);
		this.objectMapper = JsonMapper.builder().addModules(JacksonUtils.instantiateAvailableModules()).build();
	}

	public static Builder builder(EmbeddingModel embeddingModel) {
		return new Builder(embeddingModel);
	}

	public String getIndexName() {
		return this.indexName;
	}

	public int getBeamWidth() {
		return this.beamWidth;
	}

	public int getMaxConnections() {
		return this.maxConnections;
	}

	public int getBuckets() {
		return this.buckets;
	}

	public String getVectorSimilarityFunction() {
		return this.vectorSimilarityFunction;
	}

	public String[] getFields() {
		return this.fields;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!this.initializeSchema) {
			return;
		}
		if (!indexExists()) {
			createIndex();
		}
	}

	public boolean indexExists() {
		String indexResponse = getIndex();
		return !indexResponse.isEmpty();
	}

	@Nullable
	public String getIndex() {
		return this.client.get()
			.uri("/" + this.indexName)
			.retrieve()
			.bodyToMono(String.class)
			.onErrorReturn("")
			.block();
	}

	@Override
	public void doAdd(List<Document> documents) {
		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);
		UploadRequest upload = new UploadRequest(documents.stream()
			.map(document -> new UploadRequest.Embedding(document.getId(), embeddings.get(documents.indexOf(document)),
					DOCUMENT_FIELD, document.getText(), document.getMetadata()))
			.toList());

		String embeddingsJson = null;
		try {
			String embeddingString = this.objectMapper.writeValueAsString(upload);
			embeddingsJson = embeddingString.substring("{\"embeddings\":".length());
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(String.format("Embedding JSON parsing error: %s", e.getMessage()));
		}

		this.client.post()
			.uri("/" + this.indexName + EMBEDDINGS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(embeddingsJson)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientException.class, this::handleHttpClientException)
			.block();
	}

	@Override
	public void doDelete(List<String> idList) {
		try {
			this.client.method(HttpMethod.DELETE)
				.uri("/" + this.indexName + EMBEDDINGS)
				.body(BodyInserters.fromValue(idList))
				.retrieve()
				.bodyToMono(Void.class)
				.block();
		}
		catch (Exception e) {
			logger.warn("Error removing embedding: {}", e.getMessage(), e);
		}
	}

	@Override
	@Nullable
	public List<Document> doSimilaritySearch(SearchRequest request) {
		if (request.hasFilterExpression()) {
			throw new UnsupportedOperationException("GemFire currently does not support metadata filter expressions.");
		}
		float[] floatVector = this.embeddingModel.embed(request.getQuery());
		return this.client.post()
			.uri("/" + this.indexName + QUERY)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(new QueryRequest(floatVector, request.getTopK(), request.getTopK(), true))
			.retrieve()
			.bodyToFlux(QueryResponse.class)
			.filter(r -> r.score >= request.getSimilarityThreshold())
			.map(r -> {
				Map<String, Object> metadata = r.metadata;
				if (r.metadata == null) {
					metadata = new HashMap<>();
					metadata.put(DOCUMENT_FIELD, "--Deleted--");
				}
				metadata.put(DocumentMetadata.DISTANCE.value(), 1 - r.score);
				String content = (String) metadata.remove(DOCUMENT_FIELD);
				return Document.builder().id(r.key).text(content).metadata(metadata).score((double) r.score).build();
			})
			.collectList()
			.onErrorMap(WebClientException.class, this::handleHttpClientException)
			.block();
	}

	public void createIndex() throws JsonProcessingException {
		CreateRequest createRequest = new CreateRequest(this.indexName);
		createRequest.setBeamWidth(this.beamWidth);
		createRequest.setMaxConnections(this.maxConnections);
		createRequest.setBuckets(this.buckets);
		createRequest.setVectorSimilarityFunction(this.vectorSimilarityFunction);
		createRequest.setFields(this.fields);

		String index = this.objectMapper.writeValueAsString(createRequest);

		this.client.post()
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(index)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientException.class, this::handleHttpClientException)
			.block();
	}

	public void deleteIndex() {
		DeleteRequest deleteRequest = new DeleteRequest();
		this.client.method(HttpMethod.DELETE)
			.uri("/" + this.indexName)
			.body(BodyInserters.fromValue(deleteRequest))
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientException.class, this::handleHttpClientException)
			.block();
	}

	private Throwable handleHttpClientException(Throwable ex) {
		if (!(ex instanceof WebClientResponseException clientException)) {
			throw new RuntimeException(String.format("Got an unexpected error: %s", ex));
		}

		if (clientException.getStatusCode().equals(org.springframework.http.HttpStatus.NOT_FOUND)) {
			throw new RuntimeException(String.format("Index %s not found: %s", this.indexName, ex));
		}
		else if (clientException.getStatusCode().equals(org.springframework.http.HttpStatus.BAD_REQUEST)) {
			throw new RuntimeException(String.format("Bad Request: %s", ex));
		}
		else {
			throw new RuntimeException(String.format("Got an unexpected HTTP error: %s", ex));
		}
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
		return VectorStoreObservationContext.builder(VectorStoreProvider.GEMFIRE.value(), operationName)
			.collectionName(this.indexName)
			.dimensions(this.embeddingModel.dimensions())
			.fieldName(EMBEDDINGS);
	}

	public static class CreateRequest {

		@JsonProperty("name")
		private String indexName;

		@JsonProperty("beam-width")
		private int beamWidth;

		@JsonProperty("max-connections")
		private int maxConnections;

		@JsonProperty("vector-similarity-function")
		private String vectorSimilarityFunction;

		@JsonProperty("fields")
		private String[] fields;

		@JsonProperty("buckets")
		private int buckets;

		public CreateRequest() {
		}

		public CreateRequest(String indexName) {
			this.indexName = indexName;
		}

		public String getIndexName() {
			return this.indexName;
		}

		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}

		public int getBeamWidth() {
			return this.beamWidth;
		}

		public void setBeamWidth(int beamWidth) {
			this.beamWidth = beamWidth;
		}

		public int getMaxConnections() {
			return this.maxConnections;
		}

		public void setMaxConnections(int maxConnections) {
			this.maxConnections = maxConnections;
		}

		public String getVectorSimilarityFunction() {
			return this.vectorSimilarityFunction;
		}

		public void setVectorSimilarityFunction(String vectorSimilarityFunction) {
			this.vectorSimilarityFunction = vectorSimilarityFunction;
		}

		public String[] getFields() {
			return this.fields;
		}

		public void setFields(String[] fields) {
			this.fields = fields;
		}

		public int getBuckets() {
			return this.buckets;
		}

		public void setBuckets(int buckets) {
			this.buckets = buckets;
		}

	}

	private static final class UploadRequest {

		private final List<Embedding> embeddings;

		public List<Embedding> getEmbeddings() {
			return this.embeddings;
		}

		@JsonCreator
		UploadRequest(@JsonProperty("embeddings") List<Embedding> embeddings) {
			this.embeddings = embeddings;
		}

		private static final class Embedding {

			private final String key;

			private float[] vector;

			@JsonInclude(JsonInclude.Include.NON_NULL)
			private Map<String, Object> metadata;

			Embedding(@JsonProperty("key") String key, @JsonProperty("vector") float[] vector, String contentName,
					String content, @JsonProperty("metadata") Map<String, Object> metadata) {
				this.key = key;
				this.vector = vector;
				this.metadata = new HashMap<>(metadata);
				this.metadata.put(contentName, content);
			}

			public String getKey() {
				return this.key;
			}

			public float[] getVector() {
				return this.vector;
			}

			public Map<String, Object> getMetadata() {
				return this.metadata;
			}

		}

	}

	private static final class QueryRequest {

		@JsonProperty("vector")
		private final float[] vector;

		@JsonProperty("top-k")
		private final int k;

		@JsonProperty("k-per-bucket")
		private final int kPerBucket;

		@JsonProperty("include-metadata")
		private final boolean includeMetadata;

		QueryRequest(float[] vector, int k, int kPerBucket, boolean includeMetadata) {
			this.vector = vector;
			this.k = k;
			this.kPerBucket = kPerBucket;
			this.includeMetadata = includeMetadata;
		}

		public float[] getVector() {
			return this.vector;
		}

		public int getK() {
			return this.k;
		}

		public int getkPerBucket() {
			return this.kPerBucket;
		}

		public boolean isIncludeMetadata() {
			return this.includeMetadata;
		}

	}

	private static final class QueryResponse {

		private String key;

		private float score;

		private Map<String, Object> metadata;

		private String getContent(String field) {
			return (String) this.metadata.get(field);
		}

		public void setKey(String key) {
			this.key = key;
		}

		public void setScore(float score) {
			this.score = score;
		}

		public void setMetadata(Map<String, Object> metadata) {
			this.metadata = metadata;
		}

	}

	private static class DeleteRequest {

		@JsonProperty("delete-data")
		private boolean deleteData = true;

		DeleteRequest() {
		}

		DeleteRequest(boolean deleteData) {
			this.deleteData = deleteData;
		}

		public boolean isDeleteData() {
			return this.deleteData;
		}

		public void setDeleteData(boolean deleteData) {
			this.deleteData = deleteData;
		}

	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private String host = GemFireVectorStore.DEFAULT_HOST;

		private int port = GemFireVectorStore.DEFAULT_PORT;

		private boolean sslEnabled = GemFireVectorStore.DEFAULT_SSL_ENABLED;

		private String indexName = GemFireVectorStore.DEFAULT_INDEX_NAME;

		private int beamWidth = GemFireVectorStore.DEFAULT_BEAM_WIDTH;

		private int maxConnections = GemFireVectorStore.DEFAULT_MAX_CONNECTIONS;

		private int buckets = GemFireVectorStore.DEFAULT_BUCKETS;

		private String vectorSimilarityFunction = GemFireVectorStore.DEFAULT_SIMILARITY_FUNCTION;

		private String[] fields = GemFireVectorStore.DEFAULT_FIELDS;

		private boolean initializeSchema = false;

		private Builder(EmbeddingModel embeddingModel) {
			super(embeddingModel);
		}

		public Builder host(String host) {
			Assert.hasText(host, "host must have a value");
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			Assert.isTrue(port > 0, "port must be positive");
			this.port = port;
			return this;
		}

		public Builder sslEnabled(boolean sslEnabled) {
			this.sslEnabled = sslEnabled;
			return this;
		}

		public Builder indexName(String indexName) {
			Assert.hasText(indexName, "indexName must have a value");
			this.indexName = indexName;
			return this;
		}

		public Builder beamWidth(int beamWidth) {
			Assert.isTrue(beamWidth > 0, "beamWidth must be positive");
			Assert.isTrue(beamWidth <= GemFireVectorStore.UPPER_BOUND_BEAM_WIDTH,
					"beamWidth must be less than or equal to " + GemFireVectorStore.UPPER_BOUND_BEAM_WIDTH);
			this.beamWidth = beamWidth;
			return this;
		}

		public Builder maxConnections(int maxConnections) {
			Assert.isTrue(maxConnections > 0, "maxConnections must be positive");
			Assert.isTrue(maxConnections <= GemFireVectorStore.UPPER_BOUND_MAX_CONNECTIONS,
					"maxConnections must be less than or equal to " + GemFireVectorStore.UPPER_BOUND_MAX_CONNECTIONS);
			this.maxConnections = maxConnections;
			return this;
		}

		public Builder buckets(int buckets) {
			Assert.isTrue(buckets >= 0, "buckets must not be negative");
			this.buckets = buckets;
			return this;
		}

		public Builder vectorSimilarityFunction(String vectorSimilarityFunction) {
			Assert.hasText(vectorSimilarityFunction, "vectorSimilarityFunction must have a value");
			this.vectorSimilarityFunction = vectorSimilarityFunction;
			return this;
		}

		public Builder fields(String[] fields) {
			this.fields = fields;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		@Override
		public GemFireVectorStore build() {
			return new GemFireVectorStore(this);
		}

	}

}
