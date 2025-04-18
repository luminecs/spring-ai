package org.springframework.ai.model.bedrock.converse.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BedrockConverseProxyChatPropertiesTests {

	@Test
	public void chatOptionsTest() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
				"spring.ai.bedrock.converse.chat.options.model=MODEL_XYZ",

				"spring.ai.bedrock.converse.chat.options.max-tokens=123",
				"spring.ai.bedrock.converse.chat.options.metadata.user-id=MyUserId",
				"spring.ai.bedrock.converse.chat.options.stop_sequences=boza,koza",

				"spring.ai.bedrock.converse.chat.options.temperature=0.55",
				"spring.ai.bedrock.converse.chat.options.top-p=0.56",
				"spring.ai.bedrock.converse.chat.options.top-k=100"
				)
			// @formatter:on
			.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class))
			.run(context -> {
				var chatProperties = context.getBean(BedrockConverseProxyChatProperties.class);

				assertThat(chatProperties.getOptions().getModel()).isEqualTo("MODEL_XYZ");
				assertThat(chatProperties.getOptions().getMaxTokens()).isEqualTo(123);
				assertThat(chatProperties.getOptions().getStopSequences()).contains("boza", "koza");
				assertThat(chatProperties.getOptions().getTemperature()).isEqualTo(0.55);
				assertThat(chatProperties.getOptions().getTopP()).isEqualTo(0.56);
				assertThat(chatProperties.getOptions().getTopK()).isEqualTo(100);

			});
	}

	@Test
	public void chatCompletionDisabled() {

		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class))
			.run(context -> assertThat(context.getBeansOfType(BedrockConverseProxyChatProperties.class)).isNotEmpty());

		new ApplicationContextRunner().withPropertyValues("spring.ai.model.chat=bedrock-converse")
			.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(BedrockConverseProxyChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(BedrockProxyChatModel.class)).isNotEmpty();
			});

		new ApplicationContextRunner().withPropertyValues("spring.ai.model.chat=none")
			.withConfiguration(AutoConfigurations.of(BedrockConverseProxyChatAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(BedrockConverseProxyChatProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(BedrockProxyChatModel.class)).isEmpty();
			});
	}

}
