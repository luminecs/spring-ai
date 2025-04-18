package org.springframework.ai.vectorstore.elasticsearch.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import org.elasticsearch.client.RestClient;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = ElasticsearchRestClientAutoConfiguration.class)
@ConditionalOnClass({ ElasticsearchVectorStore.class, EmbeddingModel.class, RestClient.class })
@EnableConfigurationProperties(ElasticsearchVectorStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.ELASTICSEARCH,
		matchIfMissing = true)
public class ElasticsearchVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	ElasticsearchVectorStore vectorStore(ElasticsearchVectorStoreProperties properties, RestClient restClient,
			EmbeddingModel embeddingModel, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {
		ElasticsearchVectorStoreOptions elasticsearchVectorStoreOptions = new ElasticsearchVectorStoreOptions();

		PropertyMapper mapper = PropertyMapper.get();
		mapper.from(properties::getIndexName).whenHasText().to(elasticsearchVectorStoreOptions::setIndexName);
		mapper.from(properties::getDimensions).whenNonNull().to(elasticsearchVectorStoreOptions::setDimensions);
		mapper.from(properties::getSimilarity).whenNonNull().to(elasticsearchVectorStoreOptions::setSimilarity);

		return ElasticsearchVectorStore.builder(restClient, embeddingModel)
			.options(elasticsearchVectorStoreOptions)
			.initializeSchema(properties.isInitializeSchema())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy)
			.build();
	}

}
