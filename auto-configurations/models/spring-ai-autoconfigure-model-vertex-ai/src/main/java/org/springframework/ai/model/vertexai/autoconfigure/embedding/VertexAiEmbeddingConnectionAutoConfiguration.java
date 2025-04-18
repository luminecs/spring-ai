package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import com.google.cloud.vertexai.VertexAI;

import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@AutoConfiguration
@ConditionalOnClass(VertexAI.class)
@EnableConfigurationProperties(VertexAiEmbeddingConnectionProperties.class)
public class VertexAiEmbeddingConnectionAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public VertexAiEmbeddingConnectionDetails connectionDetails(
			VertexAiEmbeddingConnectionProperties connectionProperties) {

		Assert.hasText(connectionProperties.getProjectId(), "Vertex AI project-id must be set!");
		Assert.hasText(connectionProperties.getLocation(), "Vertex AI location must be set!");

		var connectionBuilder = VertexAiEmbeddingConnectionDetails.builder()
			.projectId(connectionProperties.getProjectId())
			.location(connectionProperties.getLocation());

		if (StringUtils.hasText(connectionProperties.getApiEndpoint())) {
			connectionBuilder.apiEndpoint(connectionProperties.getApiEndpoint());
		}

		return connectionBuilder.build();

	}

}
