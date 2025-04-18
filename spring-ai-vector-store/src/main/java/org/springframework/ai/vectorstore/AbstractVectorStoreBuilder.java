package org.springframework.ai.vectorstore;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public abstract class AbstractVectorStoreBuilder<T extends AbstractVectorStoreBuilder<T>>
		implements VectorStore.Builder<T> {

	protected final EmbeddingModel embeddingModel;

	protected ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

	@Nullable
	protected VectorStoreObservationConvention customObservationConvention;

	protected BatchingStrategy batchingStrategy = new TokenCountBatchingStrategy();

	public AbstractVectorStoreBuilder(EmbeddingModel embeddingModel) {
		Assert.notNull(embeddingModel, "EmbeddingModel must be configured");
		this.embeddingModel = embeddingModel;
	}

	public EmbeddingModel getEmbeddingModel() {
		return this.embeddingModel;
	}

	public BatchingStrategy getBatchingStrategy() {
		return this.batchingStrategy;
	}

	public ObservationRegistry getObservationRegistry() {
		return this.observationRegistry;
	}

	@Nullable
	public VectorStoreObservationConvention getCustomObservationConvention() {
		return this.customObservationConvention;
	}

	@SuppressWarnings("unchecked")
	protected T self() {
		return (T) this;
	}

	@Override
	public T observationRegistry(ObservationRegistry observationRegistry) {
		Assert.notNull(observationRegistry, "ObservationRegistry must not be null");
		this.observationRegistry = observationRegistry;
		return self();
	}

	@Override
	public T customObservationConvention(@Nullable VectorStoreObservationConvention convention) {
		this.customObservationConvention = convention;
		return self();
	}

	public T batchingStrategy(BatchingStrategy batchingStrategy) {
		Assert.notNull(batchingStrategy, "BatchingStrategy must not be null");
		this.batchingStrategy = batchingStrategy;
		return self();
	}

}
