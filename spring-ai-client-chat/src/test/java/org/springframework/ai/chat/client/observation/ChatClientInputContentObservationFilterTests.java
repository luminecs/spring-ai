package org.springframework.ai.chat.client.observation;

import java.util.Map;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.observation.ChatClientObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChatClientInputContentObservationFilterTests {

	private final ChatClientInputContentObservationFilter observationFilter = new ChatClientInputContentObservationFilter();

	@Mock
	ChatModel chatModel;

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyInputContentThenReturnOriginalContext() {
		var request = ChatClientRequest.builder().prompt(new Prompt()).build();

		var expectedContext = ChatClientObservationContext.builder().request(request).build();

		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenWithTextThenAugmentContext() {
		var request = ChatClientRequest.builder()
			.prompt(new Prompt(new SystemMessage("sample system text"), new UserMessage("sample user text")))
			.context(ChatClientAttributes.USER_PARAMS.getKey(), Map.of("up1", "upv1"))
			.context(ChatClientAttributes.SYSTEM_PARAMS.getKey(), Map.of("sp1", "sp1v"))
			.build();

		var originalContext = ChatClientObservationContext.builder().request(request).build();

		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(HighCardinalityKeyNames.CHAT_CLIENT_USER_TEXT.asString(), "sample user text"));
		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(HighCardinalityKeyNames.CHAT_CLIENT_USER_PARAMS.asString(), "[\"up1\":\"upv1\"]"));
		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(HighCardinalityKeyNames.CHAT_CLIENT_SYSTEM_TEXT.asString(), "sample system text"));
		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(HighCardinalityKeyNames.CHAT_CLIENT_SYSTEM_PARAM.asString(), "[\"sp1\":\"sp1v\"]"));
	}

}
