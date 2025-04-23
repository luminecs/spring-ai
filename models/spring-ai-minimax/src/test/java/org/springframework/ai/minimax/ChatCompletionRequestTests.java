package org.springframework.ai.minimax;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.minimax.api.MockWeatherService;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatCompletionRequestTests {

	@Test
	public void createRequestWithChatOptions() {

		var client = new MiniMaxChatModel(new MiniMaxApi("TEST"),
				MiniMaxChatOptions.builder().model("DEFAULT_MODEL").temperature(66.6).build());

		var request = client.createRequest(new Prompt("Test message content"), false);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isFalse();

		assertThat(request.model()).isEqualTo("DEFAULT_MODEL");
		assertThat(request.temperature()).isEqualTo(66.6);

		request = client.createRequest(new Prompt("Test message content",
				MiniMaxChatOptions.builder().model("PROMPT_MODEL").temperature(99.9).build()), true);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isTrue();

		assertThat(request.model()).isEqualTo("PROMPT_MODEL");
		assertThat(request.temperature()).isEqualTo(99.9);
	}

	@Test
	public void promptOptionsTools() {

		final String TOOL_FUNCTION_NAME = "CurrentWeather";

		var client = new MiniMaxChatModel(new MiniMaxApi("TEST"),
				MiniMaxChatOptions.builder().model("DEFAULT_MODEL").build());

		var request = client.createRequest(new Prompt("Test message content", MiniMaxChatOptions.builder()
			.model("PROMPT_MODEL")
			.functionCallbacks(List.of(FunctionToolCallback.builder(TOOL_FUNCTION_NAME, new MockWeatherService())
				.description("Get the weather in location")
				.inputType(MockWeatherService.Request.class)
				.build()))
			.build()), false);

		assertThat(client.getFunctionCallbackRegister()).hasSize(1);
		assertThat(client.getFunctionCallbackRegister()).containsKeys(TOOL_FUNCTION_NAME);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isFalse();
		assertThat(request.model()).isEqualTo("PROMPT_MODEL");

		assertThat(request.tools()).hasSize(1);
		assertThat(request.tools().get(0).getFunction().getName()).isEqualTo(TOOL_FUNCTION_NAME);
	}

	@Test
	public void defaultOptionsTools() {

		final String TOOL_FUNCTION_NAME = "CurrentWeather";

		var client = new MiniMaxChatModel(new MiniMaxApi("TEST"),
				MiniMaxChatOptions.builder()
					.model("DEFAULT_MODEL")
					.functionCallbacks(List.of(FunctionCallback.builder()
						.function(TOOL_FUNCTION_NAME, new MockWeatherService())
						.description("Get the weather in location")
						.inputType(MockWeatherService.Request.class)
						.build()))
					.build());

		var request = client.createRequest(new Prompt("Test message content"), false);

		assertThat(client.getFunctionCallbackRegister()).hasSize(1);
		assertThat(client.getFunctionCallbackRegister()).containsKeys(TOOL_FUNCTION_NAME);
		assertThat(client.getFunctionCallbackRegister().get(TOOL_FUNCTION_NAME).getDescription())
			.isEqualTo("Get the weather in location");

		assertThat(request.messages()).hasSize(1);
		assertThat(request.stream()).isFalse();
		assertThat(request.model()).isEqualTo("DEFAULT_MODEL");

		assertThat(request.tools()).as("Default Options callback functions are not automatically enabled!")
			.isNullOrEmpty();

		request = client.createRequest(
				new Prompt("Test message content", MiniMaxChatOptions.builder().function(TOOL_FUNCTION_NAME).build()),
				false);

		assertThat(request.tools()).hasSize(1);
		assertThat(request.tools().get(0).getFunction().getName()).as("Explicitly enabled function")
			.isEqualTo(TOOL_FUNCTION_NAME);

		request = client.createRequest(new Prompt("Test message content",
				MiniMaxChatOptions.builder()
					.functionCallbacks(List.of(FunctionCallback.builder()
						.function(TOOL_FUNCTION_NAME, new MockWeatherService())
						.description("Overridden function description")
						.inputType(MockWeatherService.Request.class)
						.build()))
					.build()),
				false);

		assertThat(request.tools()).hasSize(1);
		assertThat(request.tools().get(0).getFunction().getName()).as("Explicitly enabled function")
			.isEqualTo(TOOL_FUNCTION_NAME);

		assertThat(client.getFunctionCallbackRegister()).hasSize(1);
		assertThat(client.getFunctionCallbackRegister()).containsKeys(TOOL_FUNCTION_NAME);
		assertThat(client.getFunctionCallbackRegister().get(TOOL_FUNCTION_NAME).getDescription())
			.isEqualTo("Overridden function description");
	}

}
