package org.springframework.ai.model.vertexai.autoconfigure.gemini;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(VertexAiGeminiConnectionProperties.CONFIG_PREFIX)
public class VertexAiGeminiConnectionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vertex.ai.gemini";

	private String projectId;

	private String location;

	private Resource credentialsUri;

	private String apiEndpoint;

	private List<String> scopes = List.of();

	private Transport transport = Transport.GRPC;

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

	public List<String> getScopes() {
		return this.scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public Transport getTransport() {
		return this.transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public enum Transport {

		REST,

		GRPC

	}

}
