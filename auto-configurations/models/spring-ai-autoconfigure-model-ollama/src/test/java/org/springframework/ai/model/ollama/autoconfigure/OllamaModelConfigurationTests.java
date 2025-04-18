package org.springframework.ai.model.ollama.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	@Test
	void chatModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaChatAutoConfiguration.class)).run(context -> {
			assertThat(context.getBeansOfType(OllamaChatProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(OllamaChatModel.class)).isNotEmpty();
		});

		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=none")
			.run(context -> {
				assertThat(context.getBeansOfType(OllamaChatProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(OllamaChatModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=ollama")
			.run(context -> {
				assertThat(context.getBeansOfType(OllamaChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(OllamaChatModel.class)).isNotEmpty();
				assertThat(context.getBeansOfType(OllamaEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(OllamaEmbeddingModel.class)).isEmpty();
			});
	}

	@Test
	void embeddingModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaEmbeddingAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(OllamaEmbeddingProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(OllamaEmbeddingModel.class)).isNotEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=none")
			.run(context -> {
				assertThat(context.getBeansOfType(OllamaEmbeddingProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(OllamaEmbeddingModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(OllamaEmbeddingAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.embedding=ollama")
			.run(context -> {
				assertThat(context.getBeansOfType(OllamaEmbeddingProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(OllamaEmbeddingModel.class)).isNotEmpty();
			});
	}

}
