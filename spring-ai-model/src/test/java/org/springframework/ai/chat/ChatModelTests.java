package org.springframework.ai.chat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ChatModelTests {

	@Test
	void generateWithStringCallsGenerateWithPromptAndReturnsResponseCorrectly() {

		String userMessage = "Zero Wing";
		String responseMessage = "All your bases are belong to us";

		ChatModel mockClient = Mockito.mock(ChatModel.class);

		AssistantMessage mockAssistantMessage = Mockito.mock(AssistantMessage.class);
		given(mockAssistantMessage.getText()).willReturn(responseMessage);

		Generation generation = Mockito.mock(Generation.class);
		given(generation.getOutput()).willReturn(mockAssistantMessage);

		ChatResponse response = Mockito.mock(ChatResponse.class);
		given(response.getResult()).willReturn(generation);

		doCallRealMethod().when(mockClient).call(anyString());

		given(mockClient.call(any(Prompt.class))).willAnswer(invocationOnMock -> {
			Prompt prompt = invocationOnMock.getArgument(0);

			assertThat(prompt).isNotNull();
			assertThat(prompt.getContents()).isEqualTo(userMessage);

			return response;
		});

		assertThat(mockClient.call(userMessage)).isEqualTo(responseMessage);

		verify(mockClient, times(1)).call(eq(userMessage));
		verify(mockClient, times(1)).call(isA(Prompt.class));
		verify(response, times(1)).getResult();
		verify(generation, times(1)).getOutput();
		verify(mockAssistantMessage, times(1)).getText();
		verifyNoMoreInteractions(mockClient, generation, response);
	}

}
