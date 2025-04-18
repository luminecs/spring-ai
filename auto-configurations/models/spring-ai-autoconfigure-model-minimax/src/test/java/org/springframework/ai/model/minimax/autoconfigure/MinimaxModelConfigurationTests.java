package org.springframework.ai.model.minimax.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class MinimaxModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class))
		.withPropertyValues("spring.ai.minimax.api-key=API_KEY", "spring.ai.minimax.base-url=TEST_BASE_URL");

	@Test
	void chatModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxChatAutoConfiguration.class)).run(context -> {
			assertThat(context.getBeansOfType(MiniMaxChatProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(MiniMaxChatModel.class)).isNotEmpty();
		});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=none", "spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MiniMaxChatProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MiniMaxChatModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=minimax", "spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MiniMaxChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MiniMaxChatModel.class)).isNotEmpty();
			});
	}

	@Test
	void embeddingModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxEmbeddingAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(MiniMaxEmbeddingModel.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MiniMaxEmbeddingProperties.class)).isNotEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MiniMaxEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MiniMaxEmbeddingModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MiniMaxEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=minimax")
			.run(context -> {
				assertThat(context.getBeansOfType(MiniMaxEmbeddingProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MiniMaxEmbeddingModel.class)).isNotEmpty();
			});
	}

}
