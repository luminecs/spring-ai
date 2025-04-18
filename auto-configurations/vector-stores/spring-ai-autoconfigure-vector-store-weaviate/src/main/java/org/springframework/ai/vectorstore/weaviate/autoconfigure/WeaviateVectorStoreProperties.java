package org.springframework.ai.vectorstore.weaviate.autoconfigure;

import java.util.Map;

import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore.ConsistentLevel;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore.MetadataField;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(WeaviateVectorStoreProperties.CONFIG_PREFIX)
public class WeaviateVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.weaviate";

	private String scheme = "http";

	private String host = "localhost:8080";

	private String apiKey = "";

	private String objectClass = "SpringAiWeaviate";

	private ConsistentLevel consistencyLevel = WeaviateVectorStore.ConsistentLevel.ONE;

	private Map<String, MetadataField.Type> filterField = Map.of();

	private Map<String, String> headers = Map.of();

	public String getScheme() {
		return this.scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getObjectClass() {
		return this.objectClass;
	}

	public void setObjectClass(String indexName) {
		this.objectClass = indexName;
	}

	public ConsistentLevel getConsistencyLevel() {
		return this.consistencyLevel;
	}

	public void setConsistencyLevel(ConsistentLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, MetadataField.Type> getFilterField() {
		return this.filterField;
	}

	public void setFilterField(Map<String, MetadataField.Type> filterMetadataFields) {
		this.filterField = filterMetadataFields;
	}

}
