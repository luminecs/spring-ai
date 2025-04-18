package org.springframework.ai.model.bedrock.converse.autoconfigure.tool;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockTestUtils;
import org.springframework.ai.model.bedrock.autoconfigure.RequiresAwsCredentials;
import org.springframework.ai.model.bedrock.converse.autoconfigure.BedrockConverseProxyChatAutoConfiguration;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import static org.assertj.core.api.Assertions.assertThat;

@RequiresAwsCredentials
class FunctionCallWithFunctionBeanIT {

	private final Logger logger = LoggerFactory.getLogger(FunctionCallWithFunctionBeanIT.class);

	private final ApplicationContextRunner contextRunner = BedrockTestUtils.getContextRunner()
		.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class))
		.withUserConfiguration(Config.class);

	@Test
	void functionCallTest() {

		this.contextRunner
			.withPropertyValues(
					"spring.ai.bedrock.converse.chat.options.model=" + "anthropic.claude-3-5-sonnet-20240620-v1:0")
			.run(context -> {

				BedrockProxyChatModel chatModel = context.getBean(BedrockProxyChatModel.class);

				var userMessage = new UserMessage(
						"What's the weather like in San Francisco, in Paris, France and in Tokyo, Japan? Return the temperature in Celsius.");

				ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
						ToolCallingChatOptions.builder().toolNames("weatherFunction").build()));

				logger.info("Response: {}", response);

				assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");

				response = chatModel.call(new Prompt(List.of(userMessage),
						ToolCallingChatOptions.builder().toolNames("weatherFunction3").build()));

				logger.info("Response: {}", response);

				assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");
			});
	}

	@Test
	void functionStreamTest() {

		this.contextRunner
			.withPropertyValues(
					"spring.ai.bedrock.converse.chat.options.model=" + "anthropic.claude-3-5-sonnet-20240620-v1:0")
			.run(context -> {

				BedrockProxyChatModel chatModel = context.getBean(BedrockProxyChatModel.class);

				var userMessage = new UserMessage(
						"What's the weather like in San Francisco, in Paris, France and in Tokyo, Japan? Return the temperature in Celsius.");

				Flux<ChatResponse> responses = chatModel.stream(new Prompt(List.of(userMessage),
						ToolCallingChatOptions.builder().toolNames("weatherFunction").build()));

				String content = responses.collectList()
					.block()
					.stream()
					.filter(cr -> cr.getResult() != null)
					.map(cr -> cr.getResult().getOutput().getText())
					.collect(Collectors.joining());

				logger.info("Response: {}", content);
				assertThat(content).contains("30", "10", "15");

			});
	}

	@Configuration
	static class Config {

		@Bean
		@Description("Get the weather in location. Return temperature in 36°F or 36°C format.")
		public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction() {
			return new MockWeatherService();
		}

		@Bean
		public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction3() {
			MockWeatherService weatherService = new MockWeatherService();
			return (weatherService::apply);
		}

	}

}
