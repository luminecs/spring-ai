package org.springframework.ai.chat.client.advisor.api;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdvisedResponseTests {

	@Test
	void buildAdvisedResponse() {
		AdvisedResponse advisedResponse = new AdvisedResponse(mock(ChatResponse.class), Map.of());
		assertThat(advisedResponse).isNotNull();
	}

	@Test
	void whenAdviseContextIsNullThenThrows() {
		assertThatThrownBy(() -> new AdvisedResponse(mock(ChatResponse.class), null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("adviseContext cannot be null");
	}

	@Test
	void whenAdviseContextKeysIsNullThenThrows() {
		Map<String, Object> adviseContext = new HashMap<>();
		adviseContext.put(null, "value");
		assertThatThrownBy(() -> new AdvisedResponse(mock(ChatResponse.class), adviseContext))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("adviseContext keys cannot be null");
	}

	@Test
	void whenAdviseContextValuesIsNullThenThrows() {
		Map<String, Object> adviseContext = new HashMap<>();
		adviseContext.put("key", null);
		assertThatThrownBy(() -> new AdvisedResponse(mock(ChatResponse.class), adviseContext))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("adviseContext values cannot be null");
	}

	@Test
	void whenBuildFromNullAdvisedResponseThenThrows() {
		assertThatThrownBy(() -> AdvisedResponse.from((AdvisedResponse) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("advisedResponse cannot be null");
	}

	@Test
	void buildFromAdvisedResponse() {
		AdvisedResponse advisedResponse = new AdvisedResponse(mock(ChatResponse.class), Map.of());
		AdvisedResponse.Builder builder = AdvisedResponse.from(advisedResponse);
		assertThat(builder).isNotNull();
	}

	@Test
	void whenUpdateFromNullContextThenThrows() {
		AdvisedResponse advisedResponse = new AdvisedResponse(mock(ChatResponse.class), Map.of());
		assertThatThrownBy(() -> advisedResponse.updateContext(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("contextTransform cannot be null");
	}

	@Test
	void whenConvertToAndFromChatClientResponse() {
		ChatResponse chatResponse = mock(ChatResponse.class);
		Map<String, Object> context = Map.of("key", "value");
		AdvisedResponse advisedResponse = new AdvisedResponse(chatResponse, context);

		ChatClientResponse chatClientResponse = advisedResponse.toChatClientResponse();

		AdvisedResponse newAdvisedResponse = AdvisedResponse.from(chatClientResponse);
		assertThat(newAdvisedResponse).isEqualTo(advisedResponse);
	}

}
