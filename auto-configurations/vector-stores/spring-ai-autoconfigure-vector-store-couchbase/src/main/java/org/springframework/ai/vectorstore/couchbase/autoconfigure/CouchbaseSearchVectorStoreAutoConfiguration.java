package org.springframework.ai.vectorstore.couchbase.autoconfigure;

import com.couchbase.client.java.Cluster;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.CouchbaseSearchVectorStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = CouchbaseAutoConfiguration.class)
@ConditionalOnClass({ CouchbaseSearchVectorStore.class, EmbeddingModel.class, Cluster.class })
@EnableConfigurationProperties(CouchbaseSearchVectorStoreProperties.class)
public class CouchbaseSearchVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public CouchbaseSearchVectorStore vectorStore(CouchbaseSearchVectorStoreProperties properties, Cluster cluster,
			EmbeddingModel embeddingModel) {
		var builder = CouchbaseSearchVectorStore.builder(cluster, embeddingModel);

		PropertyMapper mapper = PropertyMapper.get();
		mapper.from(properties::getIndexName).whenHasText().to(builder::vectorIndexName);
		mapper.from(properties::getBucketName).whenHasText().to(builder::bucketName);
		mapper.from(properties::getScopeName).whenHasText().to(builder::scopeName);
		mapper.from(properties::getCollectionName).whenHasText().to(builder::collectionName);
		mapper.from(properties::getDimensions).whenNonNull().to(builder::dimensions);
		mapper.from(properties::getSimilarity).whenNonNull().to(builder::similarityFunction);
		mapper.from(properties::getOptimization).whenNonNull().to(builder::indexOptimization);

		return builder.initializeSchema(properties.isInitializeSchema()).build();
	}

}
