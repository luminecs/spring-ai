package org.springframework.ai.model.ollama.autoconfigure;

import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OllamaChatProperties.CONFIG_PREFIX)
public class OllamaChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.ollama.chat";

	@NestedConfigurationProperty
	private OllamaOptions options = OllamaOptions.builder().model(OllamaModel.MISTRAL.id()).build();

	public String getModel() {
		return this.options.getModel();
	}

	public void setModel(String model) {
		this.options.setModel(model);
	}

	public OllamaOptions getOptions() {
		return this.options;
	}

}
