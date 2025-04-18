package org.springframework.ai.chat.observation;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatModelObservationContextTests {

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = ChatModelObservationContext.builder()
			.prompt(generatePrompt())
			.provider("superprovider")
			.requestOptions(ChatOptions.builder().model("supermodel").build())
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenRequestOptionsIsNullThenThrow() {
		assertThatThrownBy(() -> ChatModelObservationContext.builder()
			.prompt(generatePrompt())
			.provider("superprovider")
			.requestOptions(null)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("requestOptions cannot be null");
	}

	private Prompt generatePrompt() {
		return new Prompt("hello");
	}

}
