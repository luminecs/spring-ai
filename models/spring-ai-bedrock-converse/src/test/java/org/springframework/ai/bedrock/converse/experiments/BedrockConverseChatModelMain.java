package org.springframework.ai.bedrock.converse.experiments;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

public final class BedrockConverseChatModelMain {

	private BedrockConverseChatModelMain() {

	}

	public static void main(String[] args) {

		String modelId = "ai21.jamba-1-5-large-v1:0";
		var prompt = new Prompt("Tell me a joke?", ChatOptions.builder().model(modelId).build());

		var chatModel = BedrockProxyChatModel.builder()
			.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
			.region(Region.US_EAST_1)
			.build();

		var chatResponse = chatModel.call(prompt);
		System.out.println(chatResponse);
	}

}
