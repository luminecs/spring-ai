package org.springframework.ai.model.ollama.autoconfigure;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = RestClientAutoConfiguration.class)
@ConditionalOnClass(OllamaEmbeddingModel.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.OLLAMA,
		matchIfMissing = true)
@EnableConfigurationProperties({ OllamaEmbeddingProperties.class, OllamaInitializationProperties.class })
@ImportAutoConfiguration(classes = { OllamaApiAutoConfiguration.class, RestClientAutoConfiguration.class,
		WebClientAutoConfiguration.class })
public class OllamaEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OllamaEmbeddingModel ollamaEmbeddingModel(OllamaApi ollamaApi, OllamaEmbeddingProperties properties,
			OllamaInitializationProperties initProperties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {
		var embeddingModelPullStrategy = initProperties.getEmbedding().isInclude()
				? initProperties.getPullModelStrategy() : PullModelStrategy.NEVER;

		var embeddingModel = OllamaEmbeddingModel.builder()
			.ollamaApi(ollamaApi)
			.defaultOptions(properties.getOptions())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.modelManagementOptions(new ModelManagementOptions(embeddingModelPullStrategy,
					initProperties.getEmbedding().getAdditionalModels(), initProperties.getTimeout(),
					initProperties.getMaxRetries()))
			.build();

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;
	}

}
