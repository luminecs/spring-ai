package org.springframework.ai.model.anthropic.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AnthropicPropertiesTests {

	@Test
	public void connectionProperties() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
					"spring.ai.anthropic.base-url=TEST_BASE_URL",
					"spring.ai.anthropic.api-key=abc123",
					"spring.ai.anthropic.version=6666",
					"spring.ai.anthropic.beta-version=7777",
					"spring.ai.anthropic.chat.options.model=MODEL_XYZ",
					"spring.ai.anthropic.chat.options.temperature=0.55")
				// @formatter:on
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
					RestClientAutoConfiguration.class, AnthropicChatAutoConfiguration.class))
			.run(context -> {
				var chatProperties = context.getBean(AnthropicChatProperties.class);
				var connectionProperties = context.getBean(AnthropicConnectionProperties.class);

				assertThat(connectionProperties.getApiKey()).isEqualTo("abc123");
				assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
				assertThat(connectionProperties.getVersion()).isEqualTo("6666");
				assertThat(connectionProperties.getBetaVersion()).isEqualTo("7777");

				assertThat(chatProperties.getOptions().getModel()).isEqualTo("MODEL_XYZ");
				assertThat(chatProperties.getOptions().getTemperature()).isEqualTo(0.55);
			});
	}

	@Test
	public void chatOptionsTest() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
				"spring.ai.anthropic.api-key=API_KEY",
				"spring.ai.anthropic.base-url=TEST_BASE_URL",

				"spring.ai.anthropic.chat.options.model=MODEL_XYZ",
				"spring.ai.anthropic.chat.options.max-tokens=123",
				"spring.ai.anthropic.chat.options.metadata.user-id=MyUserId",
				"spring.ai.anthropic.chat.options.stop_sequences=boza,koza",

				"spring.ai.anthropic.chat.options.temperature=0.55",
				"spring.ai.anthropic.chat.options.top-p=0.56",
				"spring.ai.anthropic.chat.options.top-k=100"
				)
			// @formatter:on
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
					RestClientAutoConfiguration.class, AnthropicChatAutoConfiguration.class))
			.run(context -> {
				var chatProperties = context.getBean(AnthropicChatProperties.class);
				var connectionProperties = context.getBean(AnthropicConnectionProperties.class);

				assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
				assertThat(connectionProperties.getApiKey()).isEqualTo("API_KEY");
				assertThat(chatProperties.getOptions().getModel()).isEqualTo("MODEL_XYZ");
				assertThat(chatProperties.getOptions().getMaxTokens()).isEqualTo(123);
				assertThat(chatProperties.getOptions().getStopSequences()).contains("boza", "koza");
				assertThat(chatProperties.getOptions().getTemperature()).isEqualTo(0.55);
				assertThat(chatProperties.getOptions().getTopP()).isEqualTo(0.56);
				assertThat(chatProperties.getOptions().getTopK()).isEqualTo(100);

				assertThat(chatProperties.getOptions().getMetadata().userId()).isEqualTo("MyUserId");
			});
	}

	@Test
	public void chatCompletionDisabled() {

		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
					RestClientAutoConfiguration.class, AnthropicChatAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(AnthropicChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(AnthropicChatModel.class)).isNotEmpty();
			});

		new ApplicationContextRunner().withPropertyValues("spring.ai.model.chat=anthropic")
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
					RestClientAutoConfiguration.class, AnthropicChatAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(AnthropicChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(AnthropicChatModel.class)).isNotEmpty();
			});

		new ApplicationContextRunner().withPropertyValues("spring.ai.model.chat=none")
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class,
					RestClientAutoConfiguration.class, AnthropicChatAutoConfiguration.class))
			.run(context -> assertThat(context.getBeansOfType(AnthropicChatModel.class)).isEmpty());
	}

}
