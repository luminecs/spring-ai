package org.springframework.ai.openai.image;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = OpenAiImageModelNoOpApiKeysIT.Config.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiImageModelNoOpApiKeysIT {

	@Autowired
	private OpenAiImageModel imageModel;

	@Test
	void checkNoOpKey() {
		assertThatThrownBy(() -> {
			var options = ImageOptionsBuilder.builder().height(1024).width(1024).build();

			var instructions = """
					A light cream colored mini golden doodle with a sign that contains the message "I'm on my way to BARCADE!".""";

			ImagePrompt imagePrompt = new ImagePrompt(instructions, options);

			this.imageModel.call(imagePrompt);
		}).isInstanceOf(NonTransientAiException.class);
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiImageApi openAiImageApi() {
			return OpenAiImageApi.builder().apiKey(new NoopApiKey()).build();
		}

		@Bean
		public OpenAiImageModel openAiImageModel(OpenAiImageApi openAiImageApi) {
			return new OpenAiImageModel(openAiImageApi, OpenAiImageOptions.builder().build(),
					RetryTemplate.defaultInstance(), TestObservationRegistry.create());
		}

	}

}
