package org.springframework.ai.model.azure.openai.autoconfigure;

import org.springframework.ai.azure.openai.AzureOpenAiAudioTranscriptionOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(AzureOpenAiAudioTranscriptionProperties.CONFIG_PREFIX)
public class AzureOpenAiAudioTranscriptionProperties {

	public static final String CONFIG_PREFIX = "spring.ai.azure.openai.audio.transcription";

	@NestedConfigurationProperty
	private AzureOpenAiAudioTranscriptionOptions options = AzureOpenAiAudioTranscriptionOptions.builder().build();

	public AzureOpenAiAudioTranscriptionOptions getOptions() {
		return this.options;
	}

	public void setOptions(AzureOpenAiAudioTranscriptionOptions options) {
		this.options = options;
	}

}
