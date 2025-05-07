package org.springframework.ai.chat.client.advisor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatModelCallAdvisorTests {

	@Test
	void whenChatModelIsNullThenThrow() {
		assertThatThrownBy(() -> ChatModelCallAdvisor.builder().chatModel(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("chatModel cannot be null");
	}

}
