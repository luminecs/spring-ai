package org.springframework.ai.model.tool;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultToolExecutionResultTests {

	@Test
	void whenConversationHistoryIsNullThenThrow() {
		assertThatThrownBy(() -> DefaultToolExecutionResult.builder().conversationHistory(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("conversationHistory cannot be null");
	}

	@Test
	void whenConversationHistoryHasNullElementsThenThrow() {
		var history = new ArrayList<Message>();
		history.add(null);
		assertThatThrownBy(() -> DefaultToolExecutionResult.builder().conversationHistory(history).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("conversationHistory cannot contain null elements");
	}

	@Test
	void builder() {
		var conversationHistory = new ArrayList<Message>();
		var result = DefaultToolExecutionResult.builder()
			.conversationHistory(conversationHistory)
			.returnDirect(true)
			.build();
		assertThat(result.conversationHistory()).isEqualTo(conversationHistory);
		assertThat(result.returnDirect()).isTrue();
	}

}
