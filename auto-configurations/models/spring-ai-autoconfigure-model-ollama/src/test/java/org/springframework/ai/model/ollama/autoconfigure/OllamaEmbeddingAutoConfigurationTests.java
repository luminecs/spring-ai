package org.springframework.ai.model.ollama.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaEmbeddingAutoConfigurationTests {

	@Test
	public void propertiesTest() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
			"spring.ai.ollama.base-url=TEST_BASE_URL",
				"spring.ai.ollama.embedding.options.model=MODEL_XYZ",
				"spring.ai.ollama.embedding.options.temperature=0.13",
				"spring.ai.ollama.embedding.options.topK=13"
				// @formatter:on
		)
			.withConfiguration(
					AutoConfigurations.of(RestClientAutoConfiguration.class, OllamaEmbeddingAutoConfiguration.class))
			.run(context -> {
				var embeddingProperties = context.getBean(OllamaEmbeddingProperties.class);
				var connectionProperties = context.getBean(OllamaConnectionProperties.class);

				assertThat(embeddingProperties.getModel()).isEqualTo("MODEL_XYZ");
				assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
				assertThat(embeddingProperties.getOptions().toMap()).containsKeys("temperature");
				assertThat(embeddingProperties.getOptions().toMap().get("temperature")).isEqualTo(0.13);
				assertThat(embeddingProperties.getOptions().getTopK()).isEqualTo(13);
			});
	}

}
