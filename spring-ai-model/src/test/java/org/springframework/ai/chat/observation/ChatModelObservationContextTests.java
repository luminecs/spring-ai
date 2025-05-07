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
			.prompt(generatePrompt(ChatOptions.builder().model("supermodel").build()))
			.provider("superprovider")
			.build();

		assertThat(observationContext).isNotNull();
	}

	private Prompt generatePrompt(ChatOptions chatOptions) {
		return new Prompt("hello", chatOptions);
	}

}
