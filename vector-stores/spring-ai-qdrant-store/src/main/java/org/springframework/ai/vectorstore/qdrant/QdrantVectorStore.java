package org.springframework.ai.vectorstore.qdrant;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
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
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class QdrantVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(QdrantVectorStore.class);

	public static final String DEFAULT_COLLECTION_NAME = "vector_store";

	private static final String CONTENT_FIELD_NAME = "doc_content";

	private final QdrantClient qdrantClient;

	private final String collectionName;

	private final QdrantFilterExpressionConverter filterExpressionConverter = new QdrantFilterExpressionConverter();

	private final boolean initializeSchema;

	protected QdrantVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.qdrantClient, "QdrantClient must not be null");

		this.qdrantClient = builder.qdrantClient;
		this.collectionName = builder.collectionName;
		this.initializeSchema = builder.initializeSchema;
	}

	public static Builder builder(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
		return new Builder(qdrantClient, embeddingModel);
	}

	@Override
	public void doAdd(List<Document> documents) {
		try {

			List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
					this.batchingStrategy);

			List<PointStruct> points = documents.stream()
				.map(document -> PointStruct.newBuilder()
					.setId(io.qdrant.client.PointIdFactory.id(UUID.fromString(document.getId())))
					.setVectors(io.qdrant.client.VectorsFactory.vectors(embeddings.get(documents.indexOf(document))))
					.putAllPayload(toPayload(document))
					.build())
				.toList();

			this.qdrantClient.upsertAsync(this.collectionName, points).get();
		}
		catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doDelete(List<String> documentIds) {
		try {
			List<PointId> ids = documentIds.stream()
				.map(id -> io.qdrant.client.PointIdFactory.id(UUID.fromString(id)))
				.toList();
			this.qdrantClient.deleteAsync(this.collectionName, ids).get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void doDelete(org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try {
			Filter filter = this.filterExpressionConverter.convertExpression(filterExpression);

			io.qdrant.client.grpc.Points.UpdateResult response = this.qdrantClient
				.deleteAsync(this.collectionName, filter)
				.get();

			if (response.getStatus() != io.qdrant.client.grpc.Points.UpdateStatus.Completed) {
				throw new IllegalStateException("Failed to delete documents by filter: " + response.getStatus());
			}

			logger.debug("Deleted documents matching filter expression");
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		try {
			Filter filter = (request.getFilterExpression() != null)
					? this.filterExpressionConverter.convertExpression(request.getFilterExpression())
					: Filter.getDefaultInstance();

			float[] queryEmbedding = this.embeddingModel.embed(request.getQuery());

			var searchPoints = SearchPoints.newBuilder()
				.setCollectionName(this.collectionName)
				.setLimit(request.getTopK())
				.setWithPayload(io.qdrant.client.WithPayloadSelectorFactory.enable(true))
				.addAllVector(EmbeddingUtils.toList(queryEmbedding))
				.setFilter(filter)
				.setScoreThreshold((float) request.getSimilarityThreshold())
				.build();

			var queryResponse = this.qdrantClient.searchAsync(searchPoints).get();

			return queryResponse.stream().map(this::toDocument).toList();

		}
		catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	private Document toDocument(ScoredPoint point) {
		try {
			var id = point.getId().getUuid();

			var metadata = QdrantObjectFactory.toObjectMap(point.getPayloadMap());
			metadata.put(DocumentMetadata.DISTANCE.value(), 1 - point.getScore());

			var content = (String) metadata.remove(CONTENT_FIELD_NAME);

			return Document.builder().id(id).text(content).metadata(metadata).score((double) point.getScore()).build();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Value> toPayload(Document document) {
		try {
			var payload = QdrantValueFactory.toValueMap(document.getMetadata());
			payload.put(CONTENT_FIELD_NAME, io.qdrant.client.ValueFactory.value(document.getText()));
			return payload;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (!this.initializeSchema) {
			return;
		}

		if (!isCollectionExists()) {
			var vectorParams = VectorParams.newBuilder()
				.setDistance(Distance.Cosine)
				.setSize(this.embeddingModel.dimensions())
				.build();
			this.qdrantClient.createCollectionAsync(this.collectionName, vectorParams).get();
		}
	}

	private boolean isCollectionExists() {
		try {
			return this.qdrantClient.listCollectionsAsync().get().stream().anyMatch(c -> c.equals(this.collectionName));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.QDRANT.value(), operationName)
			.dimensions(this.embeddingModel.dimensions())
			.collectionName(this.collectionName);

	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.qdrantClient;
		return Optional.of(client);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final QdrantClient qdrantClient;

		private String collectionName = DEFAULT_COLLECTION_NAME;

		private boolean initializeSchema = false;

		private Builder(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(qdrantClient, "QdrantClient must not be null");
			this.qdrantClient = qdrantClient;
		}

		public Builder collectionName(String collectionName) {
			Assert.hasText(collectionName, "collectionName must not be empty");
			this.collectionName = collectionName;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		@Override
		public QdrantVectorStore build() {
			return new QdrantVectorStore(this);
		}

	}

}
