package org.springframework.ai.model.zhipuai.autoconfigure.tool;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiChatAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "ZHIPU_AI_API_KEY", matches = ".*")
public class FunctionCallbackInPromptIT {

	private final Logger logger = LoggerFactory.getLogger(FunctionCallbackInPromptIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.zhipuai.apiKey=" + System.getenv("ZHIPU_AI_API_KEY"))
		.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
				RestClientAutoConfiguration.class, ZhiPuAiChatAutoConfiguration.class));

	@Test
	void functionCallTest() {
		this.contextRunner.withPropertyValues("spring.ai.zhipuai.chat.options.model=glm-4").run(context -> {

			ZhiPuAiChatModel chatModel = context.getBean(ZhiPuAiChatModel.class);

			UserMessage userMessage = new UserMessage(
					"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius.");

			var promptOptions = ZhiPuAiChatOptions.builder()
				.toolCallbacks(List.of(FunctionToolCallback.builder("CurrentWeatherService", new MockWeatherService())
					.description("Get the weather in location")
					.inputType(MockWeatherService.Request.class)

					.build()))
				.build();

			ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), promptOptions));

			logger.info("Response: {}", response);

			assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");
		});
	}

	@Test
	void streamingFunctionCallTest() {

		this.contextRunner.withPropertyValues("spring.ai.zhipuai.chat.options.model=glm-4").run(context -> {

			ZhiPuAiChatModel chatModel = context.getBean(ZhiPuAiChatModel.class);

			UserMessage userMessage = new UserMessage(
					"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius.");

			var promptOptions = ZhiPuAiChatOptions.builder()
				.toolCallbacks(List.of(FunctionToolCallback.builder("CurrentWeatherService", new MockWeatherService())
					.description("Get the weather in location")
					.inputType(MockWeatherService.Request.class)
					.build()))
				.build();

			Flux<ChatResponse> response = chatModel.stream(new Prompt(List.of(userMessage), promptOptions));

			String content = response.collectList()
				.block()
				.stream()
				.map(ChatResponse::getResults)
				.flatMap(List::stream)
				.map(Generation::getOutput)
				.map(AssistantMessage::getText)
				.collect(Collectors.joining());
			logger.info("Response: {}", content);

			assertThat(content).containsAnyOf("30.0", "30");
			assertThat(content).containsAnyOf("10.0", "10");
			assertThat(content).containsAnyOf("15.0", "15");
		});
	}

}
