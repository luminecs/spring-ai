package org.springframework.ai.chat.client.advisor.api;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdvisedRequestTests {

	@Test
	void buildAdvisedRequest() {
		AdvisedRequest request = new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of());
		assertThat(request).isNotNull();
	}

	@Test
	void whenChatModelIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(null, "user", null, null, List.of(), List.of(), List.of(),
				List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("chatModel cannot be null");
	}

	@Test
	void whenUserTextIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), null, null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(
					"userText cannot be null or empty unless messages are provided and contain Tool Response message.");
	}

	@Test
	void whenUserTextIsEmptyThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(
					"userText cannot be null or empty unless messages are provided and contain Tool Response message.");
	}

	@Test
	void whenMediaIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, null, List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("media cannot be null");
	}

	@Test
	void whenFunctionNamesIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), null,
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("functionNames cannot be null");
	}

	@Test
	void whenFunctionCallbacksIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				null, List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("functionCallbacks cannot be null");
	}

	@Test
	void whenMessagesIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), null, Map.of(), Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("messages cannot be null");
	}

	@Test
	void whenUserParamsIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), null, Map.of(), List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userParams cannot be null");
	}

	@Test
	void whenSystemParamsIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), null, List.of(), Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("systemParams cannot be null");
	}

	@Test
	void whenAdvisorsIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), null, Map.of(), Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("advisors cannot be null");
	}

	@Test
	void whenAdvisorParamsIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), null, Map.of(), Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("advisorParams cannot be null");
	}

	@Test
	void whenAdviseContextIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), null, Map.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("adviseContext cannot be null");
	}

	@Test
	void whenToolContextIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedRequest(mock(ChatModel.class), "user", null, null, List.of(), List.of(),
				List.of(), List.of(), Map.of(), Map.of(), List.of(), Map.of(), Map.of(), null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("toolContext cannot be null");
	}

}
