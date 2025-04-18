package org.springframework.ai.qianfan;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.qianfan.api.QianFanApi;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatCompletionRequestTests {

	@Test
	public void createRequestWithChatOptions() {

		var client = new QianFanChatModel(new QianFanApi("TEST", "TEST"),
				QianFanChatOptions.builder().model("DEFAULT_MODEL").temperature(66.6).build());

		var request = client.createRequest(new Prompt("Test message content"), false);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isFalse();

		assertThat(request.model()).isEqualTo("DEFAULT_MODEL");
		assertThat(request.temperature()).isEqualTo(66.6);

		request = client.createRequest(new Prompt("Test message content",
				QianFanChatOptions.builder().model("PROMPT_MODEL").temperature(99.9).build()), true);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isTrue();

		assertThat(request.model()).isEqualTo("PROMPT_MODEL");
		assertThat(request.temperature()).isEqualTo(99.9);
	}

}
