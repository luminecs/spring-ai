package org.springframework.ai.vectorstore.hanadb.autoconfigure;

import javax.sql.DataSource;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.hanadb.HanaCloudVectorStore;
import org.springframework.ai.vectorstore.hanadb.HanaVectorEntity;
import org.springframework.ai.vectorstore.hanadb.HanaVectorRepository;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = { JpaRepositoriesAutoConfiguration.class })
@ConditionalOnClass({ HanaCloudVectorStore.class, DataSource.class, HanaVectorEntity.class })
@EnableConfigurationProperties(HanaCloudVectorStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.HANADB,
		matchIfMissing = true)
public class HanaCloudVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public HanaCloudVectorStore vectorStore(HanaVectorRepository<? extends HanaVectorEntity> repository,
			EmbeddingModel embeddingModel, HanaCloudVectorStoreProperties properties,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention) {

		return HanaCloudVectorStore.builder(repository, embeddingModel)
			.tableName(properties.getTableName())
			.topK(properties.getTopK())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.build();
	}

}
