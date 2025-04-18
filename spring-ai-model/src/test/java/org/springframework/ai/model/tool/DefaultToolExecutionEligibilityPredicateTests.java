package org.springframework.ai.model.tool;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallingOptions;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultToolExecutionEligibilityPredicateTests {

	private final DefaultToolExecutionEligibilityPredicate predicate = new DefaultToolExecutionEligibilityPredicate();

	@Test
	void whenToolExecutionEnabledAndHasToolCalls() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(true).build();

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id1", "function", "testTool", "{}");
		AssistantMessage assistantMessage = new AssistantMessage("test", Map.of(), List.of(toolCall));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isTrue();
	}

	@Test
	void whenToolExecutionEnabledAndNoToolCalls() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(true).build();

		AssistantMessage assistantMessage = new AssistantMessage("test");
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isFalse();
	}

	@Test
	void whenToolExecutionDisabledAndHasToolCalls() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id1", "function", "testTool", "{}");
		AssistantMessage assistantMessage = new AssistantMessage("test", Map.of(), List.of(toolCall));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isFalse();
	}

	@Test
	void whenToolExecutionDisabledAndNoToolCalls() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();

		AssistantMessage assistantMessage = new AssistantMessage("test");
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isFalse();
	}

	@Test
	void whenFunctionCallingOptionsAndToolExecutionEnabled() {

		FunctionCallingOptions options = FunctionCallingOptions.builder().proxyToolCalls(false).build();

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id1", "function", "testTool", "{}");
		AssistantMessage assistantMessage = new AssistantMessage("test", Map.of(), List.of(toolCall));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isTrue();
	}

	@Test
	void whenFunctionCallingOptionsAndToolExecutionDisabled() {

		FunctionCallingOptions options = FunctionCallingOptions.builder().proxyToolCalls(true).build();

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id1", "function", "testTool", "{}");
		AssistantMessage assistantMessage = new AssistantMessage("test", Map.of(), List.of(toolCall));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isFalse();
	}

	@Test
	void whenRegularChatOptionsAndHasToolCalls() {

		ChatOptions options = ChatOptions.builder().build();

		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall("id1", "function", "testTool", "{}");
		AssistantMessage assistantMessage = new AssistantMessage("test", Map.of(), List.of(toolCall));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isTrue();
	}

	@Test
	void whenNullChatResponse() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(true).build();

		boolean result = this.predicate.test(options, null);
		assertThat(result).isFalse();
	}

	@Test
	void whenEmptyGenerationsList() {

		ToolCallingChatOptions options = ToolCallingChatOptions.builder().internalToolExecutionEnabled(true).build();

		ChatResponse chatResponse = new ChatResponse(List.of());

		boolean result = this.predicate.test(options, chatResponse);
		assertThat(result).isFalse();
	}

}
