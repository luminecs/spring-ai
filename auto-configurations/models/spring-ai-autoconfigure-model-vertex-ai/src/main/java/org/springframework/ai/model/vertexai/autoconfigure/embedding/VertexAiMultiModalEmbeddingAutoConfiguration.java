package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import java.io.IOException;

import com.google.cloud.vertexai.VertexAI;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.multimodal.VertexAiMultimodalEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = { SpringAiRetryAutoConfiguration.class })
@ConditionalOnClass({ VertexAI.class, VertexAiMultimodalEmbeddingModel.class })
@ConditionalOnProperty(name = SpringAIModelProperties.MULTI_MODAL_EMBEDDING_MODEL,
		havingValue = SpringAIModels.VERTEX_AI, matchIfMissing = true)
@EnableConfigurationProperties(VertexAiMultimodalEmbeddingProperties.class)
@ImportAutoConfiguration(
		classes = { SpringAiRetryAutoConfiguration.class, VertexAiEmbeddingConnectionAutoConfiguration.class })
public class VertexAiMultiModalEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public VertexAiMultimodalEmbeddingModel multimodalEmbedding(VertexAiEmbeddingConnectionDetails connectionDetails,
			VertexAiMultimodalEmbeddingProperties multimodalEmbeddingProperties) throws IOException {

		return new VertexAiMultimodalEmbeddingModel(connectionDetails, multimodalEmbeddingProperties.getOptions());
	}

}
