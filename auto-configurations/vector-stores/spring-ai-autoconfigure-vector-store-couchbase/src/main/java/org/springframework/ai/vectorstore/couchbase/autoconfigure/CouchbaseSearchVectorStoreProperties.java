package org.springframework.ai.vectorstore.couchbase.autoconfigure;

import org.springframework.ai.vectorstore.CouchbaseIndexOptimization;
import org.springframework.ai.vectorstore.CouchbaseSimilarityFunction;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = CouchbaseSearchVectorStoreProperties.CONFIG_PREFIX)
public class CouchbaseSearchVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.couchbase";

	private String indexName;

	private String collectionName;

	private String scopeName;

	private String bucketName;

	private Integer dimensions;

	private CouchbaseSimilarityFunction similarity;

	private CouchbaseIndexOptimization optimization;

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getScopeName() {
		return this.scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	public String getBucketName() {
		return this.bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public Integer getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public CouchbaseSimilarityFunction getSimilarity() {
		return this.similarity;
	}

	public void setSimilarity(CouchbaseSimilarityFunction similarity) {
		this.similarity = similarity;
	}

	public CouchbaseIndexOptimization getOptimization() {
		return this.optimization;
	}

	public void setOptimization(CouchbaseIndexOptimization optimization) {
		this.optimization = optimization;
	}

}
