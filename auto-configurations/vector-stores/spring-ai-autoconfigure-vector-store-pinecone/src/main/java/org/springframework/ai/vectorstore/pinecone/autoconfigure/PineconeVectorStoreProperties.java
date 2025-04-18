package org.springframework.ai.vectorstore.pinecone.autoconfigure;

import java.time.Duration;

import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(PineconeVectorStoreProperties.CONFIG_PREFIX)
public class PineconeVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.pinecone";

	private String apiKey;

	private String environment = "gcp-starter";

	private String projectId;

	private String indexName;

	private String namespace = "";

	private String contentFieldName = PineconeVectorStore.CONTENT_FIELD_NAME;

	private String distanceMetadataFieldName = DocumentMetadata.DISTANCE.value();

	private Duration serverSideTimeout = Duration.ofSeconds(20);

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getEnvironment() {
		return this.environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Duration getServerSideTimeout() {
		return this.serverSideTimeout;
	}

	public void setServerSideTimeout(Duration serverSideTimeout) {
		this.serverSideTimeout = serverSideTimeout;
	}

	public String getContentFieldName() {
		return this.contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getDistanceMetadataFieldName() {
		return this.distanceMetadataFieldName;
	}

	public void setDistanceMetadataFieldName(String distanceMetadataFieldName) {
		this.distanceMetadataFieldName = distanceMetadataFieldName;
	}

}
