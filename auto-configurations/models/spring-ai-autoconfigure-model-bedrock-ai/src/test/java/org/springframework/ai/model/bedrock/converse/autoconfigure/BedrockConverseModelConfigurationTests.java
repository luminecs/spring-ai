package org.springframework.ai.model.bedrock.converse.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BedrockConverseModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class));

	@Test
	void chatModelActivation() {
		this.contextRunner.run(context -> assertThat(context.getBeansOfType(BedrockProxyChatModel.class)).isNotEmpty());

		this.contextRunner.withPropertyValues("spring.ai.model.chat=none").run(context -> {
			assertThat(context.getBeansOfType(BedrockConverseProxyChatProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(BedrockProxyChatModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.chat=bedrock-converse").run(context -> {
			assertThat(context.getBeansOfType(BedrockConverseProxyChatProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(BedrockProxyChatModel.class)).isNotEmpty();
		});
	}

}
