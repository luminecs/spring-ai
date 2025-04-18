package org.springframework.ai.vectorstore.hanadb;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.EmbeddingUtils;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class HanaCloudVectorStore extends AbstractObservationVectorStore {

	private static final Logger logger = LoggerFactory.getLogger(HanaCloudVectorStore.class);

	private final HanaVectorRepository<? extends HanaVectorEntity> repository;

	private final String tableName;

	private final int topK;

	private final ObjectMapper objectMapper;

	protected HanaCloudVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.repository, "Repository must not be null");

		this.repository = builder.repository;
		this.tableName = builder.tableName;
		this.topK = builder.topK;
		this.objectMapper = JsonMapper.builder().addModules(JacksonUtils.instantiateAvailableModules()).build();
	}

	public static Builder builder(HanaVectorRepository<? extends HanaVectorEntity> repository,
			EmbeddingModel embeddingModel) {
		return new Builder(repository, embeddingModel);
	}

	@Override
	public void doAdd(List<Document> documents) {
		int count = 1;
		for (Document document : documents) {
			logger.info("[{}/{}] Calling EmbeddingModel for document id = {}", count++, documents.size(),
					document.getId());
			String content = document.getText().replaceAll("\\s+", " ");
			String embedding = getEmbedding(document);
			this.repository.save(this.tableName, document.getId(), embedding, content);
		}
		logger.info("Embeddings saved in HanaCloudVectorStore for {} documents", count - 1);
	}

	@Override
	public void doDelete(List<String> idList) {
		int deleteCount = this.repository.deleteEmbeddingsById(this.tableName, idList);
		logger.info("{} embeddings deleted", deleteCount);
	}

	public int purgeEmbeddings() {
		int deleteCount = this.repository.deleteAllEmbeddings(this.tableName);
		logger.info("{} embeddings deleted", deleteCount);
		return deleteCount;
	}

	@Override
	public List<Document> similaritySearch(String query) {
		return similaritySearch(SearchRequest.builder().query(query).topK(this.topK).build());
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		if (request.hasFilterExpression()) {
			throw new UnsupportedOperationException(
					"SAPHanaVectorEngine does not support metadata filter expressions yet.");
		}

		String queryEmbedding = getEmbedding(request);
		List<? extends HanaVectorEntity> searchResult = this.repository.cosineSimilaritySearch(this.tableName,
				request.getTopK(), queryEmbedding);
		logger.info("Hana cosine-similarity for query={}, with topK={} returned {} results", request.getQuery(),
				request.getTopK(), searchResult.size());

		return searchResult.stream().map(c -> {
			try {
				return new Document(c.get_id(), this.objectMapper.writeValueAsString(c), Collections.emptyMap());
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());
	}

	private String getEmbedding(SearchRequest searchRequest) {
		return "[" + EmbeddingUtils.toList(this.embeddingModel.embed(searchRequest.getQuery()))
			.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(", ")) + "]";
	}

	private String getEmbedding(Document document) {
		return "[" + EmbeddingUtils.toList(this.embeddingModel.embed(document))
			.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(", ")) + "]";
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.HANA.value(), operationName)
			.dimensions(this.embeddingModel.dimensions())
			.collectionName(this.tableName)
			.similarityMetric(VectorStoreSimilarityMetric.COSINE.value());
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final HanaVectorRepository<? extends HanaVectorEntity> repository;

		@Nullable
		private String tableName;

		private int topK;

		private Builder(HanaVectorRepository<? extends HanaVectorEntity> repository, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(repository, "Repository must not be null");
			this.repository = repository;
		}

		public Builder tableName(String tableName) {
			this.tableName = tableName;
			return this;
		}

		public Builder topK(int topK) {
			this.topK = topK;
			return this;
		}

		@Override
		public HanaCloudVectorStore build() {
			return new HanaCloudVectorStore(this);
		}

	}

}
