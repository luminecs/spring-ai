package org.springframework.ai.vectorstore.qdrant.autoconfigure;

import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(QdrantVectorStoreProperties.CONFIG_PREFIX)
public class QdrantVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.qdrant";

	private String collectionName = QdrantVectorStore.DEFAULT_COLLECTION_NAME;

	private String host = "localhost";

	private int port = 6334;

	private boolean useTls = false;

	private String apiKey = null;

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isUseTls() {
		return this.useTls;
	}

	public void setUseTls(boolean useTls) {
		this.useTls = useTls;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
