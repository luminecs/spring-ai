package org.springframework.ai.chat.client;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatClientResponseTests {

	@Test
	void whenContextIsNullThenThrow() {
		assertThatThrownBy(() -> new ChatClientResponse(null, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context cannot be null");

		assertThatThrownBy(() -> ChatClientResponse.builder().chatResponse(null).context(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context cannot be null");
	}

	@Test
	void whenContextHasNullKeysThenThrow() {
		Map<String, Object> context = new HashMap<>();
		context.put(null, "something");
		assertThatThrownBy(() -> new ChatClientResponse(null, context)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("context keys cannot be null");
	}

}
