package org.springframework.ai.model.tool;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

import static org.assertj.core.api.Assertions.assertThat;

class ToolExecutionResultTests {

	@Test
	void whenSingleToolCallThenSingleGeneration() {
		var toolExecutionResult = ToolExecutionResult.builder()
			.conversationHistory(List.of(new AssistantMessage("Hello, how can I help you?"),
					new UserMessage("I would like to know the weather in London"),
					new AssistantMessage("Call the weather tool"),
					new ToolResponseMessage(List.of(new ToolResponseMessage.ToolResponse("42", "weather",
							"The weather in London is 20 degrees Celsius")))))
			.build();

		var generations = ToolExecutionResult.buildGenerations(toolExecutionResult);

		assertThat(generations).hasSize(1);
		assertThat(generations.get(0).getOutput().getText()).isEqualTo("The weather in London is 20 degrees Celsius");
		assertThat((String) generations.get(0).getMetadata().get(ToolExecutionResult.METADATA_TOOL_NAME))
			.isEqualTo("weather");
		assertThat(generations.get(0).getMetadata().getFinishReason()).isEqualTo(ToolExecutionResult.FINISH_REASON);
	}

	@Test
	void whenMultipleToolCallsThenMultipleGenerations() {
		var toolExecutionResult = ToolExecutionResult.builder()
			.conversationHistory(List.of(new AssistantMessage("Hello, how can I help you?"),
					new UserMessage("I would like to know the weather in London"),
					new AssistantMessage("Call the weather tool and the news tool"),
					new ToolResponseMessage(List.of(
							new ToolResponseMessage.ToolResponse("42", "weather",
									"The weather in London is 20 degrees Celsius"),
							new ToolResponseMessage.ToolResponse("21", "news",
									"There is heavy traffic in the centre of London")))))
			.build();

		var generations = ToolExecutionResult.buildGenerations(toolExecutionResult);

		assertThat(generations).hasSize(2);
		assertThat(generations.get(0).getOutput().getText()).isEqualTo("The weather in London is 20 degrees Celsius");
		assertThat((String) generations.get(0).getMetadata().get(ToolExecutionResult.METADATA_TOOL_NAME))
			.isEqualTo("weather");
		assertThat(generations.get(0).getMetadata().getFinishReason()).isEqualTo(ToolExecutionResult.FINISH_REASON);

		assertThat(generations.get(1).getOutput().getText())
			.isEqualTo("There is heavy traffic in the centre of London");
		assertThat((String) generations.get(1).getMetadata().get(ToolExecutionResult.METADATA_TOOL_NAME))
			.isEqualTo("news");
		assertThat(generations.get(1).getMetadata().getFinishReason()).isEqualTo(ToolExecutionResult.FINISH_REASON);
	}

}
