package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(VertexAiEmbeddingConnectionProperties.CONFIG_PREFIX)
public class VertexAiEmbeddingConnectionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vertex.ai.embedding";

	private String projectId;

	private String location;

	private Resource credentialsUri;

	private String apiEndpoint;

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Resource getCredentialsUri() {
		return this.credentialsUri;
	}

	public void setCredentialsUri(Resource credentialsUri) {
		this.credentialsUri = credentialsUri;
	}

	public String getApiEndpoint() {
		return this.apiEndpoint;
	}

	public void setApiEndpoint(String apiEndpoint) {
		this.apiEndpoint = apiEndpoint;
	}

}
