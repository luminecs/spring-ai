package org.springframework.ai.vectorstore.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.Version;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestClient;
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
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class ElasticsearchVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchVectorStore.class);

	private static final Map<SimilarityFunction, VectorStoreSimilarityMetric> SIMILARITY_TYPE_MAPPING = Map.of(
			SimilarityFunction.cosine, VectorStoreSimilarityMetric.COSINE, SimilarityFunction.l2_norm,
			VectorStoreSimilarityMetric.EUCLIDEAN, SimilarityFunction.dot_product, VectorStoreSimilarityMetric.DOT);

	private final ElasticsearchClient elasticsearchClient;

	private final ElasticsearchVectorStoreOptions options;

	private final FilterExpressionConverter filterExpressionConverter;

	private final boolean initializeSchema;

	protected ElasticsearchVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.restClient, "RestClient must not be null");

		this.initializeSchema = builder.initializeSchema;
		this.options = builder.options;
		this.filterExpressionConverter = builder.filterExpressionConverter;

		String version = Version.VERSION == null ? "Unknown" : Version.VERSION.toString();
		this.elasticsearchClient = new ElasticsearchClient(new RestClientTransport(builder.restClient,
				new JacksonJsonpMapper(
						new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))))
			.withTransportOptions(t -> t.addHeader("user-agent", "spring-ai elastic-java/" + version));
	}

	@Override
	public void doAdd(List<Document> documents) {

		if (!indexExists()) {
			throw new IllegalArgumentException("Index not found");
		}
		BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		for (int i = 0; i < embeddings.size(); i++) {
			Document document = documents.get(i);
			float[] embedding = embeddings.get(i);
			bulkRequestBuilder.operations(op -> op.index(idx -> idx.index(this.options.getIndexName())
				.id(document.getId())
				.document(getDocument(document, embedding, this.options.getEmbeddingFieldName()))));
		}
		BulkResponse bulkRequest = bulkRequest(bulkRequestBuilder.build());
		if (bulkRequest.errors()) {
			List<BulkResponseItem> bulkResponseItems = bulkRequest.items();
			for (BulkResponseItem bulkResponseItem : bulkResponseItems) {
				if (bulkResponseItem.error() != null) {
					throw new IllegalStateException(bulkResponseItem.error().reason());
				}
			}
		}
	}

	private Object getDocument(Document document, float[] embedding, String embeddingFieldName) {
		Assert.notNull(document.getText(), "document's text must not be null");

		return Map.of("id", document.getId(), "content", document.getText(), "metadata", document.getMetadata(),
				embeddingFieldName, embedding);
	}

	@Override
	public void doDelete(List<String> idList) {
		BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

		if (!indexExists()) {
			throw new IllegalArgumentException("Index not found");
		}
		for (String id : idList) {
			bulkRequestBuilder.operations(op -> op.delete(idx -> idx.index(this.options.getIndexName()).id(id)));
		}
		if (bulkRequest(bulkRequestBuilder.build()).errors()) {
			throw new IllegalStateException("Delete operation failed");
		}
	}

	@Override
	public void doDelete(Filter.Expression filterExpression) {

		if (!indexExists()) {
			throw new IllegalArgumentException("Index not found");
		}

		try {
			this.elasticsearchClient.deleteByQuery(d -> d.index(this.options.getIndexName())
				.query(q -> q.queryString(qs -> qs.query(getElasticsearchQueryString(filterExpression)))));
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	private BulkResponse bulkRequest(BulkRequest bulkRequest) {
		try {
			return this.elasticsearchClient.bulk(bulkRequest);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest searchRequest) {
		Assert.notNull(searchRequest, "The search request must not be null.");
		try {
			float threshold = (float) searchRequest.getSimilarityThreshold();

			if (this.options.getSimilarity().equals(SimilarityFunction.l2_norm)) {
				threshold = 1 - threshold;
			}
			final float finalThreshold = threshold;
			float[] vectors = this.embeddingModel.embed(searchRequest.getQuery());

			SearchResponse<Document> res = this.elasticsearchClient.search(sr -> sr.index(this.options.getIndexName())
				.knn(knn -> knn.queryVector(EmbeddingUtils.toList(vectors))
					.similarity(finalThreshold)
					.k(searchRequest.getTopK())
					.field(this.options.getEmbeddingFieldName())
					.numCandidates((int) (1.5 * searchRequest.getTopK()))
					.filter(fl -> fl
						.queryString(qs -> qs.query(getElasticsearchQueryString(searchRequest.getFilterExpression())))))
				.size(searchRequest.getTopK()), Document.class);

			return res.hits().hits().stream().map(this::toDocument).collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getElasticsearchQueryString(Filter.Expression filterExpression) {
		return Objects.isNull(filterExpression) ? "*"
				: this.filterExpressionConverter.convertExpression(filterExpression);

	}

	private Document toDocument(Hit<Document> hit) {
		Document document = hit.source();
		Document.Builder documentBuilder = document.mutate();
		if (hit.score() != null) {
			documentBuilder.metadata(DocumentMetadata.DISTANCE.value(), 1 - normalizeSimilarityScore(hit.score()));
			documentBuilder.score(normalizeSimilarityScore(hit.score()));
		}
		return documentBuilder.build();
	}

	private double normalizeSimilarityScore(double score) {
		switch (this.options.getSimilarity()) {
			case l2_norm:

				return (1 - (java.lang.Math.sqrt((1 / score) - 1)));

			default:
				return (2 * score) - 1;
		}
	}

	public boolean indexExists() {
		try {
			return this.elasticsearchClient.indices().exists(ex -> ex.index(this.options.getIndexName())).value();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void createIndexMapping() {
		try {
			this.elasticsearchClient.indices()
				.create(cr -> cr.index(this.options.getIndexName())
					.mappings(map -> map.properties(this.options.getEmbeddingFieldName(),
							p -> p.denseVector(dv -> dv.similarity(this.options.getSimilarity().toString())
								.dims(this.options.getDimensions())))));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() {
		if (!this.initializeSchema) {
			return;
		}
		if (!indexExists()) {
			createIndexMapping();
		}
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {
		return VectorStoreObservationContext.builder(VectorStoreProvider.ELASTICSEARCH.value(), operationName)
			.collectionName(this.options.getIndexName())
			.dimensions(this.embeddingModel.dimensions())
			.similarityMetric(getSimilarityMetric());
	}

	private String getSimilarityMetric() {
		if (!SIMILARITY_TYPE_MAPPING.containsKey(this.options.getSimilarity())) {
			return this.options.getSimilarity().name();
		}
		return SIMILARITY_TYPE_MAPPING.get(this.options.getSimilarity()).value();
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.elasticsearchClient;
		return Optional.of(client);
	}

	public static Builder builder(RestClient restClient, EmbeddingModel embeddingModel) {
		return new Builder(restClient, embeddingModel);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final RestClient restClient;

		private ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();

		private boolean initializeSchema = false;

		private FilterExpressionConverter filterExpressionConverter = new ElasticsearchAiSearchFilterExpressionConverter();

		public Builder(RestClient restClient, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(restClient, "RestClient must not be null");
			this.restClient = restClient;
		}

		public Builder options(ElasticsearchVectorStoreOptions options) {
			Assert.notNull(options, "options must not be null");
			this.options = options;
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		public Builder filterExpressionConverter(FilterExpressionConverter converter) {
			Assert.notNull(converter, "filterExpressionConverter must not be null");
			this.filterExpressionConverter = converter;
			return this;
		}

		@Override
		public ElasticsearchVectorStore build() {
			return new ElasticsearchVectorStore(this);
		}

	}

}
