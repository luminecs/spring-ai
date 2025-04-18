package org.springframework.ai.vectorstore.neo4j.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import org.neo4j.driver.Driver;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = Neo4jAutoConfiguration.class)
@ConditionalOnClass({ Neo4jVectorStore.class, EmbeddingModel.class, Driver.class })
@EnableConfigurationProperties({ Neo4jVectorStoreProperties.class })
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.NEO4J,
		matchIfMissing = true)
public class Neo4jVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public Neo4jVectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel,
			Neo4jVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		return Neo4jVectorStore.builder(driver, embeddingModel)
			.initializeSchema(properties.isInitializeSchema())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy)
			.databaseName(properties.getDatabaseName())
			.embeddingDimension(properties.getEmbeddingDimension())
			.distanceType(properties.getDistanceType())
			.label(properties.getLabel())
			.embeddingProperty(properties.getEmbeddingProperty())
			.indexName(properties.getIndexName())
			.idProperty(properties.getIdProperty())
			.constraintName(properties.getConstraintName())
			.build();
	}

}
