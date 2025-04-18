package org.springframework.ai.model.anthropic.autoconfigure;

import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(AnthropicConnectionProperties.CONFIG_PREFIX)
public class AnthropicConnectionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.anthropic";

	private String apiKey;

	private String baseUrl = AnthropicApi.DEFAULT_BASE_URL;

	private String version = AnthropicApi.DEFAULT_ANTHROPIC_VERSION;

	private String betaVersion = AnthropicApi.DEFAULT_ANTHROPIC_BETA_VERSION;

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

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getBetaVersion() {
		return this.betaVersion;
	}

	public void setBetaVersion(String betaVersion) {
		this.betaVersion = betaVersion;
	}

}
