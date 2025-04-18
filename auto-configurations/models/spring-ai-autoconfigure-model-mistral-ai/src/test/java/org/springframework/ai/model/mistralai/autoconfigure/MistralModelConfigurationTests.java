package org.springframework.ai.model.mistralai.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class MistralModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.mistralai.apiKey=" + System.getenv("MISTRAL_AI_API_KEY"));

	@Test
	void chatModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(MistralAiChatAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(MistralAiChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MistralAiChatModel.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MistralAiChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=none", "spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MistralAiChatProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MistralAiChatModel.class)).isEmpty();
			});

		this.contextRunner
			.withConfiguration(AutoConfigurations.of(MistralAiChatAutoConfiguration.class,
					MistralAiEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=mistral", "spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MistralAiChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MistralAiChatModel.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingModel.class)).isEmpty();
			});
	}

	@Test
	void embeddingModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(MistralAiEmbeddingAutoConfiguration.class))
			.run(context -> assertThat(context.getBeansOfType(MistralAiEmbeddingModel.class)).isNotEmpty());

		this.contextRunner.withConfiguration(AutoConfigurations.of(MistralAiEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(MistralAiEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(MistralAiEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=mistral")
			.run(context -> {
				assertThat(context.getBeansOfType(MistralAiEmbeddingProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(MistralAiEmbeddingModel.class)).isNotEmpty();
			});
	}

}
