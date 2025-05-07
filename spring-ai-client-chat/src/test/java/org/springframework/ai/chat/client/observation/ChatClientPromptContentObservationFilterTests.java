package org.springframework.ai.chat.client.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatClientPromptContentObservationFilterTests {

	private final ChatClientPromptContentObservationFilter observationFilter = new ChatClientPromptContentObservationFilter();

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyPromptThenReturnOriginalContext() {
		var expectedContext = ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder().prompt(new Prompt(List.of())).build())
			.build();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenPromptWithTextThenAugmentContext() {
		var originalContext = ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder().prompt(new Prompt("supercalifragilisticexpialidocious")).build())
			.build();

		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(ChatClientObservationDocumentation.HighCardinalityKeyNames.PROMPT.asString(), """
					["user":"supercalifragilisticexpialidocious"]"""));
	}

	@Test
	void whenPromptWithMessagesThenAugmentContext() {
		var originalContext = ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder()
				.prompt(new Prompt(List.of(new SystemMessage("you're a chimney sweep"),
						new UserMessage("supercalifragilisticexpialidocious"))))
				.build())
			.build();

		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(ChatModelObservationDocumentation.HighCardinalityKeyNames.PROMPT.asString(), """
					["system":"you're a chimney sweep", "user":"supercalifragilisticexpialidocious"]"""));
	}

}
