package org.springframework.ai.anthropic;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AnthropicChatModelAdditionalHttpHeadersIT.Config.class)
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
public class AnthropicChatModelAdditionalHttpHeadersIT {

	@Autowired
	private AnthropicChatModel chatModel;

	@Test
	void additionalApiKeyHeader() {

		assertThatThrownBy(() -> this.chatModel.call("Tell me a joke")).isInstanceOf(NonTransientAiException.class);

		AnthropicChatOptions options = AnthropicChatOptions.builder()
			.httpHeaders(Map.of("x-api-key", System.getenv("ANTHROPIC_API_KEY")))
			.build();

		ChatResponse response = this.chatModel.call(new Prompt("Tell me a joke", options));

		assertThat(response).isNotNull();
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public AnthropicApi anthropicApi() {
			return new AnthropicApi("Invalid API Key");
		}

		@Bean
		public AnthropicChatModel anthropicChatModel(AnthropicApi api) {
			return AnthropicChatModel.builder().anthropicApi(api).build();
		}

	}

}
