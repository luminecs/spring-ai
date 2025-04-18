package org.springframework.ai.model.huggingface.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(HuggingfaceChatProperties.CONFIG_PREFIX)
public class HuggingfaceChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.huggingface.chat";

	private String apiKey;

	private String url;

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
