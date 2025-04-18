package org.springframework.ai.bedrock.converse.experiments;

import java.util.List;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.bedrock.converse.MockWeatherService;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;

public final class BedrockConverseChatModelMain3 {

	private BedrockConverseChatModelMain3() {

	}

	public static void main(String[] args) {

		String modelId = "anthropic.claude-3-5-sonnet-20240620-v1:0";

		var prompt = new Prompt(

				"What's the weather like in Paris? Return the temperature in Celsius.",
				ToolCallingChatOptions.builder()
					.model(modelId)
					.toolCallbacks(List.of(FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
						.description("Get the weather in location")
						.inputType(MockWeatherService.Request.class)
						.build()))
					.build());

		BedrockProxyChatModel chatModel = BedrockProxyChatModel.builder()
			.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
			.region(Region.US_EAST_1)
			.build();

		var response = chatModel.call(prompt);

		System.out.println(response);

	}

}
