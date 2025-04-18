package org.springframework.ai.openai.moderation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.api.OpenAiModerationApi;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = OpenAiModerationModelNoOpApiKeysIT.Config.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiModerationModelNoOpApiKeysIT {

	@Autowired
	private OpenAiModerationModel moderationModel;

	@Test
	void checkNoOpKey() {
		assertThatThrownBy(() -> {
			ModerationPrompt prompt = new ModerationPrompt("I want to kill them..");

			this.moderationModel.call(prompt);
		}).isInstanceOf(NonTransientAiException.class);
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiModerationApi moderationGenerationApi() {
			return OpenAiModerationApi.builder().apiKey(new NoopApiKey()).build();
		}

		@Bean
		public OpenAiModerationModel openAiModerationClient(OpenAiModerationApi openAiModerationApi) {
			return new OpenAiModerationModel(openAiModerationApi);
		}

	}

}
