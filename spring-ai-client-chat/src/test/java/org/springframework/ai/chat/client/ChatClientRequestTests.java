package org.springframework.ai.chat.client;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatClientRequestTests {

	@Test
	void whenPromptIsNullThenThrow() {
		assertThatThrownBy(() -> new ChatClientRequest(null, Map.of())).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("prompt cannot be null");

		assertThatThrownBy(() -> ChatClientRequest.builder().prompt(null).context(Map.of()).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("prompt cannot be null");
	}

	@Test
	void whenContextIsNullThenThrow() {
		assertThatThrownBy(() -> new ChatClientRequest(new Prompt(), null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context cannot be null");

		assertThatThrownBy(() -> ChatClientRequest.builder().prompt(new Prompt()).context(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context cannot be null");
	}

	@Test
	void whenContextHasNullKeysThenThrow() {
		Map<String, Object> context = new HashMap<>();
		context.put(null, "something");
		assertThatThrownBy(() -> new ChatClientRequest(new Prompt(), context))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context keys cannot be null");
	}

	@Test
	void whenCopyThenImmutableContext() {
		Map<String, Object> context = new HashMap<>();
		context.put("key", "value");
		ChatClientRequest request = ChatClientRequest.builder().prompt(new Prompt()).context(context).build();

		ChatClientRequest copy = request.copy();

		copy.context().put("key", "newValue");
		assertThat(request.context()).isEqualTo(Map.of("key", "value"));
	}

	@Test
	void whenMutateThenImmutableContext() {
		Map<String, Object> context = new HashMap<>();
		context.put("key", "value");
		ChatClientRequest request = ChatClientRequest.builder().prompt(new Prompt()).context(context).build();

		ChatClientRequest copy = request.mutate().context("key", "newValue").build();

		assertThat(request.context()).isEqualTo(Map.of("key", "value"));
		assertThat(copy.context()).isEqualTo(Map.of("key", "newValue"));
	}

}
