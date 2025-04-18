package org.springframework.ai.vectorstore.gemfire.autoconfigure;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.gemfire.GemFireVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({ GemFireVectorStore.class, EmbeddingModel.class })
@EnableConfigurationProperties(GemFireVectorStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.GEMFIRE,
		matchIfMissing = true)
public class GemFireVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(GemFireConnectionDetails.class)
	GemFireVectorStoreAutoConfiguration.PropertiesGemFireConnectionDetails gemfireConnectionDetails(
			GemFireVectorStoreProperties properties) {
		return new GemFireVectorStoreAutoConfiguration.PropertiesGemFireConnectionDetails(properties);
	}

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public GemFireVectorStore gemfireVectorStore(EmbeddingModel embeddingModel, GemFireVectorStoreProperties properties,
			GemFireConnectionDetails gemFireConnectionDetails, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		return GemFireVectorStore.builder(embeddingModel)
			.host(gemFireConnectionDetails.getHost())
			.port(gemFireConnectionDetails.getPort())
			.indexName(properties.getIndexName())
			.beamWidth(properties.getBeamWidth())
			.maxConnections(properties.getMaxConnections())
			.buckets(properties.getBuckets())
			.vectorSimilarityFunction(properties.getVectorSimilarityFunction())
			.fields(properties.getFields())
			.sslEnabled(properties.isSslEnabled())
			.initializeSchema(properties.isInitializeSchema())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy)
			.build();
	}

	private static class PropertiesGemFireConnectionDetails implements GemFireConnectionDetails {

		private final GemFireVectorStoreProperties properties;

		PropertiesGemFireConnectionDetails(GemFireVectorStoreProperties properties) {
			this.properties = properties;
		}

		@Override
		public String getHost() {
			return this.properties.getHost();
		}

		@Override
		public int getPort() {
			return this.properties.getPort();
		}

	}

}
