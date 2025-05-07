package org.springframework.ai.chat.observation;

import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.chat.observation.ChatModelObservationDocumentation.HighCardinalityKeyNames;

class ChatModelCompletionObservationFilterTests {

	private final ChatModelCompletionObservationFilter observationFilter = new ChatModelCompletionObservationFilter();

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyResponseThenReturnOriginalContext() {
		var expectedContext = ChatModelObservationContext.builder()
			.prompt(generatePrompt(ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyCompletionThenReturnOriginalContext() {
		var expectedContext = ChatModelObservationContext.builder()
			.prompt(generatePrompt(ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		expectedContext.setResponse(new ChatResponse(List.of(new Generation(new AssistantMessage("")))));
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenCompletionWithTextThenAugmentContext() {
		var originalContext = ChatModelObservationContext.builder()
			.prompt(generatePrompt(ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		originalContext.setResponse(new ChatResponse(List.of(new Generation(new AssistantMessage("say please")),
				new Generation(new AssistantMessage("seriously, say please")))));
		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues()).contains(KeyValue
			.of(HighCardinalityKeyNames.COMPLETION.asString(), "[\"say please\", \"seriously, say please\"]"));
	}

	private Prompt generatePrompt(ChatOptions chatOptions) {
		return new Prompt("supercalifragilisticexpialidocious", chatOptions);
	}

}
