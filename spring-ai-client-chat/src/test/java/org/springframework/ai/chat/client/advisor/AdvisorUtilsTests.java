package org.springframework.ai.chat.client.advisor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdvisorUtilsTests {

	@Nested
	class OnFinishReason {

		@Test
		void whenChatResponseIsNullThenReturnFalse() {
			ChatClientResponse chatClientResponse = mock(ChatClientResponse.class);
			given(chatClientResponse.chatResponse()).willReturn(null);

			boolean result = AdvisorUtils.onFinishReason().test(chatClientResponse);

			assertFalse(result);
		}

		@Test
		void whenChatResponseResultsIsNullThenReturnFalse() {
			ChatClientResponse chatClientResponse = mock(ChatClientResponse.class);
			ChatResponse chatResponse = mock(ChatResponse.class);

			given(chatResponse.getResults()).willReturn(null);
			given(chatClientResponse.chatResponse()).willReturn(chatResponse);

			boolean result = AdvisorUtils.onFinishReason().test(chatClientResponse);

			assertFalse(result);
		}

		@Test
		void whenChatIsRunningThenReturnFalse() {
			ChatClientResponse chatClientResponse = mock(ChatClientResponse.class);
			ChatResponse chatResponse = mock(ChatResponse.class);

			Generation generation = new Generation(new AssistantMessage("running.."), ChatGenerationMetadata.NULL);

			given(chatResponse.getResults()).willReturn(List.of(generation));
			given(chatClientResponse.chatResponse()).willReturn(chatResponse);

			boolean result = AdvisorUtils.onFinishReason().test(chatClientResponse);

			assertFalse(result);
		}

		@Test
		void whenChatIsStopThenReturnTrue() {
			ChatClientResponse chatClientResponse = mock(ChatClientResponse.class);
			ChatResponse chatResponse = mock(ChatResponse.class);

			Generation generation = new Generation(new AssistantMessage("finish."),
					ChatGenerationMetadata.builder().finishReason("STOP").build());

			given(chatResponse.getResults()).willReturn(List.of(generation));
			given(chatClientResponse.chatResponse()).willReturn(chatResponse);

			boolean result = AdvisorUtils.onFinishReason().test(chatClientResponse);

			assertTrue(result);
		}

	}

}
