package org.springframework.ai.model.vertexai.autoconfigure.gemini;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "VERTEX_AI_GEMINI_PROJECT_ID", matches = ".*")
@EnabledIfEnvironmentVariable(named = "VERTEX_AI_GEMINI_LOCATION", matches = ".*")
public class VertexAiModelConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withPropertyValues(
			"spring.ai.vertex.ai.gemini.project-id=" + System.getenv("VERTEX_AI_GEMINI_PROJECT_ID"),
			"spring.ai.vertex.ai.gemini.location=" + System.getenv("VERTEX_AI_GEMINI_LOCATION"));

	@Test
	void chatModelActivation() {

		this.contextRunner.withConfiguration(AutoConfigurations.of(VertexAiGeminiChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=none")
			.run(context -> {
				assertThat(context.getBeansOfType(VertexAiGeminiChatProperties.class)).isEmpty();
				assertThat(context.getBeansOfType(VertexAiGeminiChatModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(VertexAiGeminiChatAutoConfiguration.class))
			.withPropertyValues("spring.ai.model.chat=vertexai")
			.run(context -> {
				assertThat(context.getBeansOfType(VertexAiGeminiChatProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(VertexAiGeminiChatModel.class)).isNotEmpty();
			});
	}

}
