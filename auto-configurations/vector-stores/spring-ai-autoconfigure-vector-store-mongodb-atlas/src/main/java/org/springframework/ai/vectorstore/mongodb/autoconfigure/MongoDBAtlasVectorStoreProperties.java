package org.springframework.ai.vectorstore.mongodb.autoconfigure;

import java.util.List;

import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MongoDBAtlasVectorStoreProperties.CONFIG_PREFIX)
public class MongoDBAtlasVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.mongodb";

	private String collectionName;

	private String pathName;

	private String indexName;

	private List<String> metadataFieldsToFilter = List.of();

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getPathName() {
		return this.pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public List<String> getMetadataFieldsToFilter() {
		return this.metadataFieldsToFilter;
	}

	public void setMetadataFieldsToFilter(List<String> metadataFieldsToFilter) {
		this.metadataFieldsToFilter = metadataFieldsToFilter;
	}

}
