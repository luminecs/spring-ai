package org.springframework.ai.vectorstore.azure.autoconfigure;

import org.springframework.ai.vectorstore.azure.AzureVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(AzureVectorStoreProperties.CONFIG_PREFIX)
public class AzureVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.azure";

	private String url;

	private String apiKey;

	private String indexName = AzureVectorStore.DEFAULT_INDEX_NAME;

	private int defaultTopK = -1;

	private double defaultSimilarityThreshold = -1;

	private boolean useKeylessAuth;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String endpointUrl) {
		this.url = endpointUrl;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public int getDefaultTopK() {
		return this.defaultTopK;
	}

	public void setDefaultTopK(int defaultTopK) {
		this.defaultTopK = defaultTopK;
	}

	public double getDefaultSimilarityThreshold() {
		return this.defaultSimilarityThreshold;
	}

	public void setDefaultSimilarityThreshold(double defaultSimilarityThreshold) {
		this.defaultSimilarityThreshold = defaultSimilarityThreshold;
	}

	public boolean isUseKeylessAuth() {
		return this.useKeylessAuth;
	}

	public void setUseKeylessAuth(boolean useKeylessAuth) {
		this.useKeylessAuth = useKeylessAuth;
	}

}
