package org.springframework.ai.anthropic.api;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.core.publisher.Flux;

import org.springframework.ai.anthropic.api.AnthropicApi.AnthropicMessage;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionRequest;
import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionResponse;
import org.springframework.ai.anthropic.api.AnthropicApi.ContentBlock;
import org.springframework.ai.anthropic.api.AnthropicApi.Role;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
public class AnthropicApiIT {

	AnthropicApi anthropicApi = AnthropicApi.builder().apiKey(System.getenv("ANTHROPIC_API_KEY")).build();

	@Test
	void chatCompletionEntity() {

		AnthropicMessage chatCompletionMessage = new AnthropicMessage(List.of(new ContentBlock("Tell me a Joke?")),
				Role.USER);
		ResponseEntity<ChatCompletionResponse> response = this.anthropicApi
			.chatCompletionEntity(new ChatCompletionRequest(AnthropicApi.ChatModel.CLAUDE_3_OPUS.getValue(),
					List.of(chatCompletionMessage), null, 100, 0.8, false));

		System.out.println(response);
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	void chatCompletionWithThinking() {
		AnthropicMessage chatCompletionMessage = new AnthropicMessage(List.of(new ContentBlock("Tell me a Joke?")),
				Role.USER);

		ChatCompletionRequest request = ChatCompletionRequest.builder()
			.model(AnthropicApi.ChatModel.CLAUDE_3_7_SONNET.getValue())
			.messages(List.of(chatCompletionMessage))
			.maxTokens(8192)
			.temperature(1.0)
			.thinking(new ChatCompletionRequest.ThinkingConfig(AnthropicApi.ThinkingType.ENABLED, 2048))
			.build();

		ResponseEntity<ChatCompletionResponse> response = this.anthropicApi.chatCompletionEntity(request);

		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();

		List<ContentBlock> content = response.getBody().content();
		for (ContentBlock block : content) {
			if (block.type() == ContentBlock.Type.THINKING) {
				assertThat(block.thinking()).isNotBlank();
				assertThat(block.signature()).isNotBlank();
			}
			if (block.type() == ContentBlock.Type.REDACTED_THINKING) {
				assertThat(block.data()).isNotBlank();
			}
			if (block.type() == ContentBlock.Type.TEXT) {
				assertThat(block.text()).isNotBlank();
			}
		}
	}

	@Test
	void chatCompletionStream() {

		AnthropicMessage chatCompletionMessage = new AnthropicMessage(List.of(new ContentBlock("Tell me a Joke?")),
				Role.USER);

		Flux<ChatCompletionResponse> response = this.anthropicApi.chatCompletionStream(new ChatCompletionRequest(
				AnthropicApi.ChatModel.CLAUDE_3_OPUS.getValue(), List.of(chatCompletionMessage), null, 100, 0.8, true));

		assertThat(response).isNotNull();

		List<ChatCompletionResponse> bla = response.collectList().block();
		assertThat(bla).isNotNull();

		bla.stream().forEach(r -> System.out.println(r));
	}

	@Test
	void chatCompletionStreamError() {
		AnthropicMessage chatCompletionMessage = new AnthropicMessage(List.of(new ContentBlock("Tell me a Joke?")),
				Role.USER);
		AnthropicApi api = AnthropicApi.builder().baseUrl("FAKE_KEY_FOR_ERROR_RESPONSE").build();

		Flux<ChatCompletionResponse> response = api.chatCompletionStream(new ChatCompletionRequest(
				AnthropicApi.ChatModel.CLAUDE_3_OPUS.getValue(), List.of(chatCompletionMessage), null, 100, 0.8, true));

		assertThat(response).isNotNull();

		assertThatThrownBy(() -> response.collectList().block()).isInstanceOf(RuntimeException.class)
			.hasMessageStartingWith("Response exception, Status: [")
			.hasMessageContaining(
					"{\"type\":\"error\",\"error\":{\"type\":\"authentication_error\",\"message\":\"invalid x-api-key\"}}");
	}

}
