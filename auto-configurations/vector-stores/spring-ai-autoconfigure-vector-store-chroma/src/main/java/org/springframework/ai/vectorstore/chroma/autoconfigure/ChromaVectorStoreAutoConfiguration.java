package org.springframework.ai.vectorstore.chroma.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass({ EmbeddingModel.class, RestClient.class, ChromaVectorStore.class, ObjectMapper.class })
@EnableConfigurationProperties({ ChromaApiProperties.class, ChromaVectorStoreProperties.class })
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.CHROMA,
		matchIfMissing = true)
public class ChromaVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(ChromaConnectionDetails.class)
	PropertiesChromaConnectionDetails chromaConnectionDetails(ChromaApiProperties properties) {
		return new PropertiesChromaConnectionDetails(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public ChromaApi chromaApi(ChromaApiProperties apiProperties,
			ObjectProvider<RestClient.Builder> restClientBuilderProvider, ChromaConnectionDetails connectionDetails,
			ObjectMapper objectMapper) {

		String chromaUrl = String.format("%s:%s", connectionDetails.getHost(), connectionDetails.getPort());

		var chromaApi = new ChromaApi(chromaUrl, restClientBuilderProvider.getIfAvailable(RestClient::builder),
				objectMapper);

		if (StringUtils.hasText(connectionDetails.getKeyToken())) {
			chromaApi.withKeyToken(connectionDetails.getKeyToken());
		}
		else if (StringUtils.hasText(apiProperties.getUsername()) && StringUtils.hasText(apiProperties.getPassword())) {
			chromaApi.withBasicAuthCredentials(apiProperties.getUsername(), apiProperties.getPassword());
		}

		return chromaApi;
	}

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy chromaBatchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public ChromaVectorStore vectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi,
			ChromaVectorStoreProperties storeProperties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy chromaBatchingStrategy) {
		return ChromaVectorStore.builder(chromaApi, embeddingModel)
			.collectionName(storeProperties.getCollectionName())
			.initializeSchema(storeProperties.isInitializeSchema())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(chromaBatchingStrategy)
			.build();
	}

	static class PropertiesChromaConnectionDetails implements ChromaConnectionDetails {

		private final ChromaApiProperties properties;

		PropertiesChromaConnectionDetails(ChromaApiProperties properties) {
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

		@Override
		public String getKeyToken() {
			return this.properties.getKeyToken();
		}

	}

}
