package org.springframework.ai.model.watsonxai.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(WatsonxAiConnectionProperties.CONFIG_PREFIX)
public class WatsonxAiConnectionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.watsonx.ai";

	private String baseUrl = "https://us-south.ml.cloud.ibm.com/";

	private String streamEndpoint = "ml/v1/text/generation_stream?version=2023-05-29";

	private String textEndpoint = "ml/v1/text/generation?version=2023-05-29";

	private String embeddingEndpoint = "ml/v1/text/embeddings?version=2023-05-29";

	private String projectId;

	private String IAMToken;

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getStreamEndpoint() {
		return this.streamEndpoint;
	}

	public void setStreamEndpoint(String streamEndpoint) {
		this.streamEndpoint = streamEndpoint;
	}

	public String getTextEndpoint() {
		return this.textEndpoint;
	}

	public void setTextEndpoint(String textEndpoint) {
		this.textEndpoint = textEndpoint;
	}

	public String getEmbeddingEndpoint() {
		return this.embeddingEndpoint;
	}

	public void setEmbeddingEndpoint(String embeddingEndpoint) {
		this.embeddingEndpoint = embeddingEndpoint;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getIAMToken() {
		return this.IAMToken;
	}

	public void setIAMToken(String IAMToken) {
		this.IAMToken = IAMToken;
	}

}
