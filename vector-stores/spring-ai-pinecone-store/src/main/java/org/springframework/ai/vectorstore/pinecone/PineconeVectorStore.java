package org.springframework.ai.vectorstore.pinecone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.QueryRequest;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
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
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.filter.converter.PineconeFilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class PineconeVectorStore extends AbstractObservationVectorStore {

	public static final String CONTENT_FIELD_NAME = "document_content";

	public final FilterExpressionConverter filterExpressionConverter = new PineconeFilterExpressionConverter();

	private final String pineconeNamespace;

	private final String pineconeIndexName;

	private final String pineconeContentFieldName;

	private final String pineconeDistanceMetadataFieldName;

	private final Pinecone pinecone;

	private final ObjectMapper objectMapper;

	private static final Logger logger = LoggerFactory.getLogger(PineconeVectorStore.class);

	protected PineconeVectorStore(Builder builder) {
		super(builder);

		Assert.hasText(builder.apiKey, "ApiKey must not be null or empty");
		Assert.hasText(builder.indexName, "IndexName must not be null or empty");

		this.pineconeNamespace = builder.namespace;
		this.pineconeIndexName = builder.indexName;
		this.pineconeContentFieldName = builder.contentFieldName;
		this.pineconeDistanceMetadataFieldName = builder.distanceMetadataFieldName;

		this.pinecone = new Pinecone.Builder(builder.apiKey).build();
		this.objectMapper = new ObjectMapper();
	}

	public static Builder.BuilderWithApiKey builder(EmbeddingModel embeddingModel) {
		return Builder.StepBuilder.start(embeddingModel);
	}

	public void add(List<Document> documents, String namespace) {
		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);
		List<VectorWithUnsignedIndices> upsertVectors = new ArrayList<>();
		for (Document document : documents) {
			upsertVectors.add(io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices(document.getId(),
					EmbeddingUtils.toList(embeddings.get(documents.indexOf(document))), null, null,
					metadataToStruct(document)));
		}
		this.pinecone.getIndexConnection(this.pineconeIndexName).upsert(upsertVectors, namespace);
	}

	@Override
	public void doAdd(List<Document> documents) {
		add(documents, this.pineconeNamespace);
	}

	private Struct metadataToStruct(Document document) {
		try {
			var structBuilder = Struct.newBuilder();
			JsonFormat.parser()
				.ignoringUnknownFields()
				.merge(this.objectMapper.writeValueAsString(document.getMetadata()), structBuilder);
			structBuilder.putFields(this.pineconeContentFieldName, contentValue(document));
			return structBuilder.build();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Value contentValue(Document document) {
		return Value.newBuilder().setStringValue(document.getText()).build();
	}

	public void delete(List<String> documentIds, String namespace) {
		this.pinecone.getIndexConnection(this.pineconeIndexName).delete(documentIds, false, namespace, null);
	}

	@Override
	public void doDelete(List<String> documentIds) {
		delete(documentIds, this.pineconeNamespace);
	}

	public List<Document> similaritySearch(SearchRequest request, String namespace) {

		String nativeExpressionFilters = (request.getFilterExpression() != null)
				? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";

		float[] queryEmbedding = this.embeddingModel.embed(request.getQuery());

		var queryRequestBuilder = QueryRequest.newBuilder()
			.addAllVector(EmbeddingUtils.toList(queryEmbedding))
			.setTopK(request.getTopK())
			.setIncludeMetadata(true)
			.setNamespace(namespace);

		if (StringUtils.hasText(nativeExpressionFilters)) {
			queryRequestBuilder.setFilter(metadataFiltersToStruct(nativeExpressionFilters));
		}

		QueryResponseWithUnsignedIndices queryResponse = this.pinecone.getIndexConnection(this.pineconeIndexName)
			.queryByVector(request.getTopK(), EmbeddingUtils.toList(queryEmbedding), namespace,
					metadataFiltersToStruct(nativeExpressionFilters), false, true);

		return queryResponse.getMatchesList()
			.stream()
			.filter(scoredVector -> scoredVector.getScore() >= request.getSimilarityThreshold())
			.map(scoredVector -> {
				var id = scoredVector.getId();
				Struct metadataStruct = scoredVector.getMetadata();
				var content = metadataStruct.getFieldsOrThrow(this.pineconeContentFieldName).getStringValue();
				Map<String, Object> metadata = extractMetadata(metadataStruct);
				metadata.put(this.pineconeDistanceMetadataFieldName, 1 - scoredVector.getScore());
				return Document.builder()
					.id(id)
					.text(content)
					.metadata(metadata)
					.score((double) scoredVector.getScore())
					.build();
			})
			.toList();
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

			List<Document> matchingDocs = similaritySearch(searchRequest, this.pineconeNamespace);

			if (!matchingDocs.isEmpty()) {

				List<String> idsToDelete = matchingDocs.stream().map(Document::getId).collect(Collectors.toList());
				delete(idsToDelete, this.pineconeNamespace);
				logger.debug("Deleted {} documents matching filter expression", idsToDelete.size());
			}
		}
		catch (Exception e) {
			logger.error("Failed to delete documents by filter", e);
			throw new IllegalStateException("Failed to delete documents by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		return similaritySearch(request, this.pineconeNamespace);
	}

	private Struct metadataFiltersToStruct(String metadataFilters) {
		try {
			if (StringUtils.hasText(metadataFilters)) {
				var structBuilder = Struct.newBuilder();
				JsonFormat.parser().ignoringUnknownFields().merge(metadataFilters, structBuilder);
				return structBuilder.build();
			}
			return null;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> extractMetadata(Struct metadataStruct) {
		try {
			String json = JsonFormat.printer().print(metadataStruct);
			Map<String, Object> metadata = this.objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {

			});
			metadata.remove(this.pineconeContentFieldName);
			return metadata;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.PINECONE.value(), operationName)
			.collectionName(this.pineconeIndexName)
			.dimensions(this.embeddingModel.dimensions())
			.namespace(this.pineconeNamespace)
			.fieldName(this.pineconeContentFieldName);
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.pinecone;
		return Optional.of(client);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final String apiKey;

		private final String indexName;

		private String namespace = "";

		private String contentFieldName = CONTENT_FIELD_NAME;

		private String distanceMetadataFieldName = DocumentMetadata.DISTANCE.value();

		private Builder(EmbeddingModel embeddingModel, String apiKey, String indexName) {
			super(embeddingModel);
			this.apiKey = apiKey;
			this.indexName = indexName;
		}

		public Builder namespace(@Nullable String namespace) {
			this.namespace = namespace != null ? namespace : "";
			return this;
		}

		public Builder contentFieldName(@Nullable String contentFieldName) {
			this.contentFieldName = contentFieldName != null ? contentFieldName : CONTENT_FIELD_NAME;
			return this;
		}

		public Builder distanceMetadataFieldName(@Nullable String distanceMetadataFieldName) {
			this.distanceMetadataFieldName = distanceMetadataFieldName != null ? distanceMetadataFieldName
					: DocumentMetadata.DISTANCE.value();
			return this;
		}

		@Override
		public PineconeVectorStore build() {
			return new PineconeVectorStore(this);
		}

		public interface BuilderWithApiKey {

			BuilderWithIndexName apiKey(String apiKey);

		}

		public interface BuilderWithIndexName {

			Builder indexName(String indexName);

		}

		public static class StepBuilder {

			static BuilderWithApiKey start(EmbeddingModel embeddingModel) {
				Assert.notNull(embeddingModel, "EmbeddingModel must not be null");
				return new ApiKeyStep(embeddingModel);
			}

			private record ApiKeyStep(EmbeddingModel embeddingModel) implements BuilderWithApiKey {
				@Override
				public BuilderWithIndexName apiKey(String apiKey) {
					Assert.hasText(apiKey, "ApiKey must not be null or empty");
					return new IndexNameStep(this.embeddingModel, apiKey);
				}
			}

			private record IndexNameStep(EmbeddingModel embeddingModel, String apiKey) implements BuilderWithIndexName {
				@Override
				public Builder indexName(String indexName) {
					Assert.hasText(indexName, "IndexName must not be null or empty");
					return new Builder(this.embeddingModel, this.apiKey, indexName);
				}
			}

		}

	}

}
