package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiImageProperties.CONFIG_PREFIX)
public class OpenAiImageProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.image";

	public static final String DEFAULT_IMAGE_MODEL = OpenAiImageApi.ImageModel.DALL_E_3.getValue();

	@NestedConfigurationProperty
	private OpenAiImageOptions options = OpenAiImageOptions.builder().model(DEFAULT_IMAGE_MODEL).build();

	public OpenAiImageOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiImageOptions options) {
		this.options = options;
	}

}
