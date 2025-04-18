package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;

@AutoConfiguration(after = { SpringAiRetryAutoConfiguration.class })
@ConditionalOnClass(VertexAiTextEmbeddingModel.class)
@ConditionalOnProperty(name = SpringAIModelProperties.TEXT_EMBEDDING_MODEL, havingValue = SpringAIModels.VERTEX_AI,
		matchIfMissing = true)
@EnableConfigurationProperties(VertexAiTextEmbeddingProperties.class)
@ImportAutoConfiguration(
		classes = { SpringAiRetryAutoConfiguration.class, VertexAiEmbeddingConnectionAutoConfiguration.class })
public class VertexAiTextEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public VertexAiTextEmbeddingModel textEmbedding(VertexAiEmbeddingConnectionDetails connectionDetails,
			VertexAiTextEmbeddingProperties textEmbeddingProperties, RetryTemplate retryTemplate,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {

		var embeddingModel = new VertexAiTextEmbeddingModel(connectionDetails, textEmbeddingProperties.getOptions(),
				retryTemplate, observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;
	}

}
