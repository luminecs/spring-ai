package org.springframework.ai.model.azure.openai.autoconfigure.tool;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_ENDPOINT", matches = ".+")
public class FunctionCallWithPromptFunctionIT {

	private final Logger logger = LoggerFactory.getLogger(FunctionCallWithPromptFunctionIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withPropertyValues(
	// @formatter:off
				"spring.ai.azure.openai.api-key=" + System.getenv("AZURE_OPENAI_API_KEY"),
				"spring.ai.azure.openai.endpoint=" + System.getenv("AZURE_OPENAI_ENDPOINT"))

		.withConfiguration(AutoConfigurations.of(AzureOpenAiChatAutoConfiguration.class));

	@Test
	void functionCallTest() {
		this.contextRunner
			.withPropertyValues(
					"spring.ai.azure.openai.chat.options.deployment-name=" + DeploymentNameUtil.getDeploymentName())
			.run(context -> {

				AzureOpenAiChatModel chatModel = context.getBean(AzureOpenAiChatModel.class);

				UserMessage userMessage = new UserMessage(
						"What's the weather like in San Francisco, in Paris and in Tokyo? Use Multi-turn function calling.");

				var promptOptions = AzureOpenAiChatOptions.builder()
					.toolCallbacks(
							List.of(FunctionToolCallback.builder("CurrentWeatherService", new MockWeatherService())
								.description("Get the weather in location")
								.inputType(MockWeatherService.Request.class)
								.build()))
					.build();

				ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), promptOptions));

				logger.info("Response: {}", response);

				assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");
			});
	}

}
