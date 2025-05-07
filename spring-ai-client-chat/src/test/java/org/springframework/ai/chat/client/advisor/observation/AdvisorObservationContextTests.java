package org.springframework.ai.chat.client.advisor.observation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdvisorObservationContextTests {

	@Test
	void whenMandatoryOptionsThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.advisorName("AdvisorName")
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void missingAdvisorName() {
		assertThatThrownBy(() -> AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("advisorName cannot be null or empty");
	}

	@Test
	void missingChatClientRequest() {
		assertThatThrownBy(() -> AdvisorObservationContext.builder().advisorName("AdvisorName").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("chatClientRequest cannot be null");
	}

	@Test
	void whenBuilderWithChatClientRequestThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.advisorName("AdvisorName")
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt()).build())
			.build();

		assertThat(observationContext).isNotNull();
	}

}
