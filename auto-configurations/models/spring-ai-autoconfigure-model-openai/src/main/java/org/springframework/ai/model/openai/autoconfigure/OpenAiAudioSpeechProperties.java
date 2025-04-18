package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiAudioSpeechProperties.CONFIG_PREFIX)
public class OpenAiAudioSpeechProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.audio.speech";

	public static final String DEFAULT_SPEECH_MODEL = OpenAiAudioApi.TtsModel.TTS_1.getValue();

	private static final Float SPEED = 1.0f;

	private static final String VOICE = OpenAiAudioApi.SpeechRequest.Voice.ALLOY.getValue();

	private static final OpenAiAudioApi.SpeechRequest.AudioResponseFormat DEFAULT_RESPONSE_FORMAT = OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3;

	@NestedConfigurationProperty
	private OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
		.model(DEFAULT_SPEECH_MODEL)
		.responseFormat(DEFAULT_RESPONSE_FORMAT)
		.voice(VOICE)
		.speed(SPEED)
		.build();

	public OpenAiAudioSpeechOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiAudioSpeechOptions options) {
		this.options = options;
	}

}
