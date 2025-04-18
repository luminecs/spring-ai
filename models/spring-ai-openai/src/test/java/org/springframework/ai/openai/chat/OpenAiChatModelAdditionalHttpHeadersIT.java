package org.springframework.ai.openai.chat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = OpenAiChatModelAdditionalHttpHeadersIT.Config.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiChatModelAdditionalHttpHeadersIT {

	@Autowired
	private OpenAiChatModel openAiChatModel;

	@Test
	void additionalApiKeyHeader() {

		assertThatThrownBy(() -> this.openAiChatModel.call("Tell me a joke"))
			.isInstanceOf(NonTransientAiException.class);

		OpenAiChatOptions options = OpenAiChatOptions.builder()
			.httpHeaders(Map.of("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY")))
			.build();

		ChatResponse response = this.openAiChatModel.call(new Prompt("Tell me a joke", options));

		assertThat(response).isNotNull();
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiApi chatCompletionApi() {
			return OpenAiApi.builder().apiKey(new SimpleApiKey("Invalid API Key")).build();
		}

		@Bean
		public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
			return OpenAiChatModel.builder().openAiApi(openAiApi).build();
		}

	}

}
