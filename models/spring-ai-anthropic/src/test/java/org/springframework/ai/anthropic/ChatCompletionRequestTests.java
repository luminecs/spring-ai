package org.springframework.ai.anthropic;

import org.junit.jupiter.api.Test;

import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatCompletionRequestTests {

	@Test
	public void createRequestWithChatOptions() {

		var client = AnthropicChatModel.builder()
			.anthropicApi(AnthropicApi.builder().apiKey("TEST").build())
			.defaultOptions(AnthropicChatOptions.builder().model("DEFAULT_MODEL").temperature(66.6).build())
			.build();

		var prompt = client.buildRequestPrompt(new Prompt("Test message content"));

		var request = client.createRequest(prompt, false);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isFalse();

		assertThat(request.model()).isEqualTo("DEFAULT_MODEL");
		assertThat(request.temperature()).isEqualTo(66.6);

		prompt = client.buildRequestPrompt(new Prompt("Test message content",
				AnthropicChatOptions.builder().model("PROMPT_MODEL").temperature(99.9).build()));

		request = client.createRequest(prompt, true);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isTrue();

		assertThat(request.model()).isEqualTo("PROMPT_MODEL");
		assertThat(request.temperature()).isEqualTo(99.9);
	}

}
