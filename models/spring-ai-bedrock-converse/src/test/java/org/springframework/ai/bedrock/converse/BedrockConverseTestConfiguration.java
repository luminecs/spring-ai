package org.springframework.ai.bedrock.converse;

import java.time.Duration;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class BedrockConverseTestConfiguration {

	@Bean
	public BedrockProxyChatModel bedrockConverseChatModel() {

		String modelId = "anthropic.claude-3-5-sonnet-20240620-v1:0";

		return BedrockProxyChatModel.builder()
			.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
			.region(Region.US_EAST_1)

			.timeout(Duration.ofSeconds(120))
			.defaultOptions(ToolCallingChatOptions.builder().model(modelId).build())
			.build();
	}

}
