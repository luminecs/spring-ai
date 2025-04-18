package org.springframework.ai.model.bedrock.titan.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BedrockTitanModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(BedrockTitanEmbeddingAutoConfiguration.class))
		.withBean(ObjectMapper.class, ObjectMapper::new);

	@Test
	void embeddingModelActivation() {
		this.contextRunner
			.run(context -> assertThat(context.getBeansOfType(BedrockTitanEmbeddingModel.class)).isNotEmpty());

		this.contextRunner.withPropertyValues("spring.ai.model.embedding=none").run(context -> {
			assertThat(context.getBeansOfType(BedrockTitanEmbeddingProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(BedrockTitanEmbeddingModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.embedding=bedrock-titan").run(context -> {
			assertThat(context.getBeansOfType(BedrockTitanEmbeddingProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(BedrockTitanEmbeddingModel.class)).isNotEmpty();
		});
	}

}
