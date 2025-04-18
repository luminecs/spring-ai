package org.springframework.ai.vectorstore.weaviate.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({ EmbeddingModel.class, WeaviateVectorStore.class })
@EnableConfigurationProperties({ WeaviateVectorStoreProperties.class })
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.WEAVIATE,
		matchIfMissing = true)
public class WeaviateVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(WeaviateConnectionDetails.class)
	public PropertiesWeaviateConnectionDetails weaviateConnectionDetails(WeaviateVectorStoreProperties properties) {
		return new PropertiesWeaviateConnectionDetails(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public WeaviateClient weaviateClient(WeaviateVectorStoreProperties properties,
			WeaviateConnectionDetails connectionDetails) {
		try {
			return WeaviateAuthClient.apiKey(
					new Config(properties.getScheme(), connectionDetails.getHost(), properties.getHeaders()),
					properties.getApiKey());
		}
		catch (AuthException e) {
			throw new IllegalArgumentException("WeaviateClient could not be created.", e);
		}
	}

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public WeaviateVectorStore vectorStore(EmbeddingModel embeddingModel, WeaviateClient weaviateClient,
			WeaviateVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
			.objectClass(properties.getObjectClass())
			.filterMetadataFields(properties.getFilterField()
				.entrySet()
				.stream()
				.map(e -> new WeaviateVectorStore.MetadataField(e.getKey(), e.getValue()))
				.toList())
			.consistencyLevel(WeaviateVectorStore.ConsistentLevel.valueOf(properties.getConsistencyLevel().name()))
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy)
			.build();
	}

	static class PropertiesWeaviateConnectionDetails implements WeaviateConnectionDetails {

		private final WeaviateVectorStoreProperties properties;

		PropertiesWeaviateConnectionDetails(WeaviateVectorStoreProperties properties) {
			this.properties = properties;
		}

		@Override
		public String getHost() {
			return this.properties.getHost();
		}

	}

}
