package org.springframework.ai.vectorstore.observation;

import java.util.List;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.lang.Nullable;

public abstract class AbstractObservationVectorStore implements VectorStore {

	private static final VectorStoreObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultVectorStoreObservationConvention();

	private final ObservationRegistry observationRegistry;

	@Nullable
	private final VectorStoreObservationConvention customObservationConvention;

	protected final EmbeddingModel embeddingModel;

	protected final BatchingStrategy batchingStrategy;

	private AbstractObservationVectorStore(EmbeddingModel embeddingModel, ObservationRegistry observationRegistry,
			@Nullable VectorStoreObservationConvention customObservationConvention, BatchingStrategy batchingStrategy) {
		this.embeddingModel = embeddingModel;
		this.observationRegistry = observationRegistry;
		this.customObservationConvention = customObservationConvention;
		this.batchingStrategy = batchingStrategy;
	}

	public AbstractObservationVectorStore(AbstractVectorStoreBuilder<?> builder) {
		this(builder.getEmbeddingModel(), builder.getObservationRegistry(), builder.getCustomObservationConvention(),
				builder.getBatchingStrategy());
	}

	@Override
	public void add(List<Document> documents) {

		VectorStoreObservationContext observationContext = this
			.createObservationContextBuilder(VectorStoreObservationContext.Operation.ADD.value())
			.build();

		VectorStoreObservationDocumentation.AI_VECTOR_STORE
			.observation(this.customObservationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> this.doAdd(documents));
	}

	@Override
	public void delete(List<String> deleteDocIds) {

		VectorStoreObservationContext observationContext = this
			.createObservationContextBuilder(VectorStoreObservationContext.Operation.DELETE.value())
			.build();

		VectorStoreObservationDocumentation.AI_VECTOR_STORE
			.observation(this.customObservationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> this.doDelete(deleteDocIds));
	}

	@Override
	public void delete(Filter.Expression filterExpression) {
		VectorStoreObservationContext observationContext = this
			.createObservationContextBuilder(VectorStoreObservationContext.Operation.DELETE.value())
			.build();

		VectorStoreObservationDocumentation.AI_VECTOR_STORE
			.observation(this.customObservationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> this.doDelete(filterExpression));
	}

	@Override
	@Nullable
	public List<Document> similaritySearch(SearchRequest request) {

		VectorStoreObservationContext searchObservationContext = this
			.createObservationContextBuilder(VectorStoreObservationContext.Operation.QUERY.value())
			.queryRequest(request)
			.build();

		return VectorStoreObservationDocumentation.AI_VECTOR_STORE
			.observation(this.customObservationConvention, DEFAULT_OBSERVATION_CONVENTION,
					() -> searchObservationContext, this.observationRegistry)
			.observe(() -> {
				var documents = this.doSimilaritySearch(request);
				searchObservationContext.setQueryResponse(documents);
				return documents;
			});
	}

	public abstract void doAdd(List<Document> documents);

	public abstract void doDelete(List<String> idList);

	protected void doDelete(Filter.Expression filterExpression) {

		throw new UnsupportedOperationException();
	}

	public abstract List<Document> doSimilaritySearch(SearchRequest request);

	public abstract VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName);

}
