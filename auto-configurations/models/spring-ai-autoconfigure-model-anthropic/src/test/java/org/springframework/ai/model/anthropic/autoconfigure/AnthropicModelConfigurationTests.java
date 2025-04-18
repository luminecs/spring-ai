package org.springframework.ai.model.anthropic.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AnthropicModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.anthropic.apiKey=" + System.getenv("ANTHROPIC_API_KEY"))
		.withConfiguration(AutoConfigurations.of(AnthropicChatAutoConfiguration.class));

	@Test
	void chatModelActivation() {
		this.contextRunner.run(context -> assertThat(context.getBeansOfType(AnthropicChatModel.class)).isNotEmpty());

		this.contextRunner.withPropertyValues("spring.ai.model.chat=none").run(context -> {
			assertThat(context.getBeansOfType(AnthropicChatProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(AnthropicChatModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.chat=anthropic").run(context -> {
			assertThat(context.getBeansOfType(AnthropicChatProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(AnthropicChatModel.class)).isNotEmpty();
		});
	}

}
