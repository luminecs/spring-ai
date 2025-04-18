package org.springframework.ai.model.mistralai.autoconfigure;

public class MistralAiParentProperties {

	private String apiKey;

	private String baseUrl;

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

}
