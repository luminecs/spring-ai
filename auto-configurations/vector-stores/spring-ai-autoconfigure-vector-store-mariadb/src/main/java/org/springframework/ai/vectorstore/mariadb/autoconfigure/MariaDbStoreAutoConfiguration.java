package org.springframework.ai.vectorstore.mariadb.autoconfigure;

import javax.sql.DataSource;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnClass({ MariaDBVectorStore.class, DataSource.class, JdbcTemplate.class })
@EnableConfigurationProperties(org.springframework.ai.vectorstore.mariadb.autoconfigure.MariaDbStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.MARIADB,
		matchIfMissing = true)
public class MariaDbStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy mariaDbStoreBatchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	public MariaDBVectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel,
			org.springframework.ai.vectorstore.mariadb.autoconfigure.MariaDbStoreProperties properties,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		var initializeSchema = properties.isInitializeSchema();

		return MariaDBVectorStore.builder(jdbcTemplate, embeddingModel)
			.schemaName(properties.getSchemaName())
			.vectorTableName(properties.getTableName())
			.schemaValidation(properties.isSchemaValidation())
			.dimensions(properties.getDimensions())
			.distanceType(properties.getDistanceType())
			.contentFieldName(properties.getContentFieldName())
			.embeddingFieldName(properties.getEmbeddingFieldName())
			.idFieldName(properties.getIdFieldName())
			.metadataFieldName(properties.getMetadataFieldName())
			.removeExistingVectorStoreTable(properties.isRemoveExistingVectorStoreTable())
			.initializeSchema(initializeSchema)
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy)
			.maxDocumentBatchSize(properties.getMaxDocumentBatchSize())
			.build();
	}

}
