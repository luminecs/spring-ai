package org.springframework.ai.model.azure.openai.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(AzureOpenAiConnectionProperties.CONFIG_PREFIX)
public class AzureOpenAiConnectionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.azure.openai";

	private String apiKey;

	private String openAiApiKey;

	private String endpoint;

	private Map<String, String> customHeaders = new HashMap<>();

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getOpenAiApiKey() {
		return this.openAiApiKey;
	}

	public void setOpenAiApiKey(String openAiApiKey) {
		this.openAiApiKey = openAiApiKey;
	}

	public Map<String, String> getCustomHeaders() {
		return this.customHeaders;
	}

	public void setCustomHeaders(Map<String, String> customHeaders) {
		this.customHeaders = customHeaders;
	}

}
