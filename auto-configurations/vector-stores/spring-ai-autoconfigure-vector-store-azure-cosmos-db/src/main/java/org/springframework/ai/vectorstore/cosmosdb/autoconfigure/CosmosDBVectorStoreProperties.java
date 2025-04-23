package org.springframework.ai.vectorstore.cosmosdb.autoconfigure;

import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(CosmosDBVectorStoreProperties.CONFIG_PREFIX)
public class CosmosDBVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.cosmosdb";

	private String containerName;

	private String databaseName;

	private String metadataFields;

	private int vectorStoreThroughput = 400;

	private long vectorDimensions = 1536;

	private String partitionKeyPath;

	private String endpoint;

	private String key;

	private String connectionMode;

	public int getVectorStoreThroughput() {
		return this.vectorStoreThroughput;
	}

	public void setVectorStoreThroughput(int vectorStoreThroughput) {
		this.vectorStoreThroughput = vectorStoreThroughput;
	}

	public String getMetadataFields() {
		return this.metadataFields;
	}

	public void setMetadataFields(String metadataFields) {
		this.metadataFields = metadataFields;
	}

	public List<String> getMetadataFieldList() {
		return this.metadataFields != null
				? Arrays.stream(this.metadataFields.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
				: List.of();
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setConnectionMode(String connectionMode) {
		this.connectionMode = connectionMode;
	}

	public String getConnectionMode() {
		return this.connectionMode;
	}

	public String getDatabaseName() {
		return this.databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getContainerName() {
		return this.containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getPartitionKeyPath() {
		return this.partitionKeyPath;
	}

	public void setPartitionKeyPath(String partitionKeyPath) {
		this.partitionKeyPath = partitionKeyPath;
	}

	public long getVectorDimensions() {
		return this.vectorDimensions;
	}

	public void setVectorDimensions(long vectorDimensions) {
		this.vectorDimensions = vectorDimensions;
	}

}
