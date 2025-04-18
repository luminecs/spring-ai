package org.springframework.ai.model.huggingface.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class HuggingfaceModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(HuggingfaceChatAutoConfiguration.class));

	@Test
	void chatModelActivation() {
		this.contextRunner.run(context -> assertThat(context.getBeansOfType(HuggingfaceChatModel.class)).isNotEmpty());

		this.contextRunner.withPropertyValues("spring.ai.model.chat=none").run(context -> {
			assertThat(context.getBeansOfType(HuggingfaceChatProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(HuggingfaceChatModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.chat=huggingface").run(context -> {
			assertThat(context.getBeansOfType(HuggingfaceChatProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(HuggingfaceChatModel.class)).isNotEmpty();
		});
	}

}
