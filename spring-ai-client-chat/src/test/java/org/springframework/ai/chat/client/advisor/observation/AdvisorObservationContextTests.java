package org.springframework.ai.chat.client.advisor.observation;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdvisorObservationContextTests {

	@Test
	void whenMandatoryOptionsThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.advisorName("AdvisorName")
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void missingAdvisorName() {
		assertThatThrownBy(() -> AdvisorObservationContext.builder().build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("advisorName cannot be null or empty");
	}

	@Test
	void whenBuilderWithAdvisedRequestThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.advisorName("AdvisorName")
			.advisedRequest(mock(AdvisedRequest.class))
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenBuilderWithChatClientRequestThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.advisorName("AdvisorName")
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt()).build())
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void missingBuilderWithBothRequestsThenThrow() {
		assertThatThrownBy(() -> AdvisorObservationContext.builder()
			.advisedRequest(mock(AdvisedRequest.class))
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt()).build())
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("ChatClientRequest and AdvisedRequest cannot be set at the same time");
	}

}
