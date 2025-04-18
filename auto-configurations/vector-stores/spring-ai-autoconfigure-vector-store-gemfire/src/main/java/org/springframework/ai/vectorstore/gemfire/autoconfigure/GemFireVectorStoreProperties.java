package org.springframework.ai.vectorstore.gemfire.autoconfigure;

import org.springframework.ai.vectorstore.gemfire.GemFireVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(GemFireVectorStoreProperties.CONFIG_PREFIX)
public class GemFireVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.gemfire";

	private String host = GemFireVectorStore.DEFAULT_HOST;

	private int port = GemFireVectorStore.DEFAULT_PORT;

	private String indexName = GemFireVectorStore.DEFAULT_INDEX_NAME;

	private int beamWidth = GemFireVectorStore.DEFAULT_BEAM_WIDTH;

	private int maxConnections = GemFireVectorStore.DEFAULT_MAX_CONNECTIONS;

	private String vectorSimilarityFunction = GemFireVectorStore.DEFAULT_SIMILARITY_FUNCTION;

	private String[] fields = GemFireVectorStore.DEFAULT_FIELDS;

	private int buckets = GemFireVectorStore.DEFAULT_BUCKETS;

	private boolean sslEnabled = GemFireVectorStore.DEFAULT_SSL_ENABLED;

	public int getBeamWidth() {
		return this.beamWidth;
	}

	public void setBeamWidth(int beamWidth) {
		this.beamWidth = beamWidth;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public int getMaxConnections() {
		return this.maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public String getVectorSimilarityFunction() {
		return this.vectorSimilarityFunction;
	}

	public void setVectorSimilarityFunction(String vectorSimilarityFunction) {
		this.vectorSimilarityFunction = vectorSimilarityFunction;
	}

	public String[] getFields() {
		return this.fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public int getBuckets() {
		return this.buckets;
	}

	public void setBuckets(int buckets) {
		this.buckets = buckets;
	}

	public boolean isSslEnabled() {
		return this.sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

}
