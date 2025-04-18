package org.springframework.ai.openai.audio.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = OpenAiAudioModelNoOpApiKeysIT.Config.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiAudioModelNoOpApiKeysIT {

	@Autowired
	private OpenAiAudioApi audioApi;

	@Test
	void checkNoOpKey() {
		assertThatThrownBy(() -> this.audioApi
			.createSpeech(OpenAiAudioApi.SpeechRequest.builder()
				.model(OpenAiAudioApi.TtsModel.TTS_1_HD.getValue())
				.input("Hello, my name is Chris and I love Spring A.I.")
				.voice(OpenAiAudioApi.SpeechRequest.Voice.ONYX.getValue())
				.build())
			.getBody()).isInstanceOf(NonTransientAiException.class);
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiAudioApi openAiAudioApi() {
			return OpenAiAudioApi.builder().apiKey(new NoopApiKey()).build();
		}

	}

}
