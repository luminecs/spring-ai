package org.springframework.ai.model.mistralai.autoconfigure.tool;

import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionRequest.ToolChoice;
import org.springframework.ai.model.mistralai.autoconfigure.MistralAiChatAutoConfiguration;
import org.springframework.ai.model.mistralai.autoconfigure.tool.WeatherServicePromptIT.MyWeatherService.Request;
import org.springframework.ai.model.mistralai.autoconfigure.tool.WeatherServicePromptIT.MyWeatherService.Response;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".*")
public class WeatherServicePromptIT {

	private final Logger logger = LoggerFactory.getLogger(WeatherServicePromptIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.mistralai.api-key=" + System.getenv("MISTRAL_AI_API_KEY"))
		.withConfiguration(AutoConfigurations.of(MistralAiChatAutoConfiguration.class));

	@Test
	void promptFunctionCall() {
		this.contextRunner
			.withPropertyValues("spring.ai.mistralai.chat.options.model=" + MistralAiApi.ChatModel.LARGE.getValue())
			.run(context -> {

				MistralAiChatModel chatModel = context.getBean(MistralAiChatModel.class);

				UserMessage userMessage = new UserMessage("What's the weather like in Paris? Use Celsius.");

				var promptOptions = MistralAiChatOptions.builder()
					.toolChoice(ToolChoice.AUTO)
					.toolCallbacks(List.of(FunctionToolCallback.builder("CurrentWeatherService", new MyWeatherService())
						.description("Get the current weather in requested location")
						.inputType(MyWeatherService.Request.class)
						.build()))
					.build();

				ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), promptOptions));

				logger.info("Response: {}", response);

				assertThat(response.getResult().getOutput().getText()).containsAnyOf("15", "15.0");
			});
	}

	@Test
	void functionCallWithPortableFunctionCallingOptions() {
		this.contextRunner
			.withPropertyValues("spring.ai.mistralai.chat.options.model=" + MistralAiApi.ChatModel.LARGE.getValue())
			.run(context -> {

				MistralAiChatModel chatModel = context.getBean(MistralAiChatModel.class);

				UserMessage userMessage = new UserMessage("What's the weather like in Paris? Use Celsius.");

				ToolCallingChatOptions functionOptions = ToolCallingChatOptions.builder()
					.toolCallbacks(List.of(FunctionToolCallback.builder("CurrentWeatherService", new MyWeatherService())
						.description("Get the current weather in requested location")
						.inputType(MyWeatherService.Request.class)
						.build()))

					.build();

				ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), functionOptions));

				logger.info("Response: {}", response);

				assertThat(response.getResult().getOutput().getText()).containsAnyOf("15", "15.0");
			});
	}

	public static class MyWeatherService implements Function<Request, Response> {

		@Override
		public Response apply(Request request) {
			if (request.location().contains("Paris")) {
				return new Response(15, request.unit());
			}
			else if (request.location().contains("Tokyo")) {
				return new Response(10, request.unit());
			}
			else if (request.location().contains("San Francisco")) {
				return new Response(30, request.unit());
			}
			throw new IllegalArgumentException("Invalid request: " + request);
		}

		// @formatter:off
		public enum Unit { C, F }

		@JsonInclude(Include.NON_NULL)
		public record Request(
				@JsonProperty(required = true, value = "location") String location,
				@JsonProperty(required = true, value = "unit") Unit unit) { }
		// @formatter:on

		public record Response(double temperature, Unit unit) {

		}

	}

}
