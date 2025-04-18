package org.springframework.ai.model.watsonxai.autoconfigure;

import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class WatsonxAiAutoConfigurationTests {

	@Test
	public void propertiesTest() {
		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
			"spring.ai.watsonx.ai.base-url=TEST_BASE_URL",
			"spring.ai.watsonx.ai.stream-endpoint=ml/v1/text/generation_stream?version=2023-05-29",
			"spring.ai.watsonx.ai.text-endpoint=ml/v1/text/generation?version=2023-05-29",
			"spring.ai.watsonx.ai.embedding-endpoint=ml/v1/text/embeddings?version=2023-05-29",
			"spring.ai.watsonx.ai.projectId=1",
			"spring.ai.watsonx.ai.IAMToken=123456")
                // @formatter:on
			.withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class,
					WatsonxAiChatAutoConfiguration.class, WatsonxAiEmbeddingAutoConfiguration.class))
			.run(context -> {
				var connectionProperties = context.getBean(WatsonxAiConnectionProperties.class);
				assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
				assertThat(connectionProperties.getStreamEndpoint())
					.isEqualTo("ml/v1/text/generation_stream?version=2023-05-29");
				assertThat(connectionProperties.getTextEndpoint())
					.isEqualTo("ml/v1/text/generation?version=2023-05-29");
				assertThat(connectionProperties.getEmbeddingEndpoint())
					.isEqualTo("ml/v1/text/embeddings?version=2023-05-29");
				assertThat(connectionProperties.getProjectId()).isEqualTo("1");
				assertThat(connectionProperties.getIAMToken()).isEqualTo("123456");
			});
	}

}
