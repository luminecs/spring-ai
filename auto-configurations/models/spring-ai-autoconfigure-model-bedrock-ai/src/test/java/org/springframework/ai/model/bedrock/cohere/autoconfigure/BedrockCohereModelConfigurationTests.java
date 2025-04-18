package org.springframework.ai.model.bedrock.cohere.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BedrockCohereModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(BedrockCohereEmbeddingAutoConfiguration.class))
		.withBean(ObjectMapper.class, ObjectMapper::new);

	@Test
	void embeddingModelActivation() {
		this.contextRunner
			.run(context -> assertThat(context.getBeansOfType(BedrockCohereEmbeddingModel.class)).isNotEmpty());

		this.contextRunner.withPropertyValues("spring.ai.model.embedding=none").run(context -> {
			assertThat(context.getBeansOfType(BedrockCohereEmbeddingProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(BedrockCohereEmbeddingModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.embedding=bedrock-cohere").run(context -> {
			assertThat(context.getBeansOfType(BedrockCohereEmbeddingProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(BedrockCohereEmbeddingModel.class)).isNotEmpty();
		});
	}

}
