package org.springframework.ai.moonshot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.moonshot.api.MoonshotApi;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MOONSHOT_API_KEY", matches = ".+")
public class MoonshotChatCompletionRequestTest {

	MoonshotChatModel chatModel = new MoonshotChatModel(new MoonshotApi("test"));

	@Test
	void chatCompletionDefaultRequestTest() {
		var request = this.chatModel.createRequest(new Prompt("test content"), false);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.topP()).isEqualTo(1);
		assertThat(request.temperature()).isEqualTo(0.7);
		assertThat(request.maxTokens()).isNull();
		assertThat(request.stream()).isFalse();
	}

	@Test
	void chatCompletionRequestWithOptionsTest() {
		var options = MoonshotChatOptions.builder().temperature(0.5).topP(0.8).build();
		var request = this.chatModel.createRequest(new Prompt("test content", options), true);

		assertThat(request.messages().size()).isEqualTo(1);
		assertThat(request.topP()).isEqualTo(0.8);
		assertThat(request.temperature()).isEqualTo(0.5);
		assertThat(request.stream()).isTrue();
	}

}
