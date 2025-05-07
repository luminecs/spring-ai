package org.springframework.ai.chat.client.observation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChatClientObservationContextTests {

	@Mock
	ChatModel chatModel;

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder().prompt(new Prompt()).build())
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenNullAdvisorsThenReturn() {
		assertThatThrownBy(() -> ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder().prompt(new Prompt()).build())
			.advisors(null)
			.build()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("advisors cannot be null");
	}

	@Test
	void whenAdvisorsWithNullElementsThenReturn() {
		List<Advisor> advisors = new ArrayList<>();
		advisors.add(mock(Advisor.class));
		advisors.add(null);
		assertThatThrownBy(() -> ChatClientObservationContext.builder()
			.request(ChatClientRequest.builder().prompt(new Prompt()).build())
			.advisors(advisors)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("advisors cannot contain null elements");
	}

}
