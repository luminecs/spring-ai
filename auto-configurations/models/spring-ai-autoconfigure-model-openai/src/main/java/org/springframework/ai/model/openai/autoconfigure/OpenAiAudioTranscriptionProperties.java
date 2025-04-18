package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiAudioTranscriptionProperties.CONFIG_PREFIX)
public class OpenAiAudioTranscriptionProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.audio.transcription";

	public static final String DEFAULT_TRANSCRIPTION_MODEL = OpenAiAudioApi.WhisperModel.WHISPER_1.getValue();

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	private static final OpenAiAudioApi.TranscriptResponseFormat DEFAULT_RESPONSE_FORMAT = OpenAiAudioApi.TranscriptResponseFormat.TEXT;

	@NestedConfigurationProperty
	private OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
		.model(DEFAULT_TRANSCRIPTION_MODEL)
		.temperature(DEFAULT_TEMPERATURE.floatValue())
		.responseFormat(DEFAULT_RESPONSE_FORMAT)
		.build();

	public OpenAiAudioTranscriptionOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiAudioTranscriptionOptions options) {
		this.options = options;
	}

}
