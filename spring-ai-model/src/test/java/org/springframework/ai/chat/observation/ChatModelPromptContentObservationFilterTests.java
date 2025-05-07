package org.springframework.ai.chat.observation;

import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.chat.observation.ChatModelObservationDocumentation.HighCardinalityKeyNames;

class ChatModelPromptContentObservationFilterTests {

	private final ChatModelPromptContentObservationFilter observationFilter = new ChatModelPromptContentObservationFilter();

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyPromptThenReturnOriginalContext() {
		var expectedContext = ChatModelObservationContext.builder()
			.prompt(new Prompt(List.of(), ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenPromptWithTextThenAugmentContext() {
		var originalContext = ChatModelObservationContext.builder()
			.prompt(new Prompt("supercalifragilisticexpialidocious", ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues()).contains(
				KeyValue.of(HighCardinalityKeyNames.PROMPT.asString(), "[\"supercalifragilisticexpialidocious\"]"));
	}

	@Test
	void whenPromptWithMessagesThenAugmentContext() {
		var originalContext = ChatModelObservationContext.builder()
			.prompt(new Prompt(
					List.of(new SystemMessage("you're a chimney sweep"),
							new UserMessage("supercalifragilisticexpialidocious")),
					ChatOptions.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(HighCardinalityKeyNames.PROMPT.asString(),
					"[\"you're a chimney sweep\", \"supercalifragilisticexpialidocious\"]"));
	}

}
