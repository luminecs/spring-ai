package org.springframework.ai.model.tool;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolExecutionEligibilityPredicateTests {

	@Test
	void whenIsToolExecutionRequiredWithNullPromptOptions() {
		ToolExecutionEligibilityPredicate predicate = new TestToolExecutionEligibilityPredicate();
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("test"))));

		assertThatThrownBy(() -> predicate.isToolExecutionRequired(null, chatResponse))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("promptOptions cannot be null");
	}

	@Test
	void whenIsToolExecutionRequiredWithNullChatResponse() {
		ToolExecutionEligibilityPredicate predicate = new TestToolExecutionEligibilityPredicate();
		ChatOptions promptOptions = ChatOptions.builder().build();

		assertThatThrownBy(() -> predicate.isToolExecutionRequired(promptOptions, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("chatResponse cannot be null");
	}

	@Test
	void whenIsToolExecutionRequiredWithValidInputs() {
		ToolExecutionEligibilityPredicate predicate = new TestToolExecutionEligibilityPredicate();
		ChatOptions promptOptions = ChatOptions.builder().build();
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("test"))));

		boolean result = predicate.isToolExecutionRequired(promptOptions, chatResponse);
		assertThat(result).isTrue();
	}

	@Test
	void whenTestMethodCalledDirectly() {
		ToolExecutionEligibilityPredicate predicate = new TestToolExecutionEligibilityPredicate();
		ChatOptions promptOptions = ChatOptions.builder().build();
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("test"))));

		boolean result = predicate.test(promptOptions, chatResponse);
		assertThat(result).isTrue();
	}

	private static class TestToolExecutionEligibilityPredicate implements ToolExecutionEligibilityPredicate {

		@Override
		public boolean test(ChatOptions promptOptions, ChatResponse chatResponse) {
			return true;
		}

	}

}
