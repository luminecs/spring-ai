package org.springframework.ai.vectorstore.cosmosdb.autoconfigure;

import java.util.List;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.cosmosdb.CosmosDBVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({ CosmosDBVectorStore.class, EmbeddingModel.class, CosmosAsyncClient.class })
@EnableConfigurationProperties(CosmosDBVectorStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.AZURE_COSMOS_DB,
		matchIfMissing = true)
public class CosmosDBVectorStoreAutoConfiguration {

	@Bean
	public CosmosAsyncClient cosmosClient(CosmosDBVectorStoreProperties properties) {
		return new CosmosClientBuilder().endpoint(properties.getEndpoint())
			.userAgentSuffix("SpringAI-CDBNoSQL-VectorStore")
			.key(properties.getKey())
			.gatewayMode()
			.buildAsyncClient();
	}

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public CosmosDBVectorStore cosmosDBVectorStore(ObservationRegistry observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			CosmosDBVectorStoreProperties properties, CosmosAsyncClient cosmosAsyncClient,
			EmbeddingModel embeddingModel, BatchingStrategy batchingStrategy) {

		return CosmosDBVectorStore.builder(cosmosAsyncClient, embeddingModel)
			.databaseName(properties.getDatabaseName())
			.containerName(properties.getContainerName())
			.metadataFields(List.of(properties.getMetadataFields()))
			.vectorStoreThroughput(properties.getVectorStoreThroughput())
			.vectorDimensions(properties.getVectorDimensions())
			.partitionKeyPath(properties.getPartitionKeyPath())
			.build();

	}

}
