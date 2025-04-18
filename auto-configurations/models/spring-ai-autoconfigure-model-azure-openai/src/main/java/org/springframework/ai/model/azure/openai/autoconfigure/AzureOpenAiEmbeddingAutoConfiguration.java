package org.springframework.ai.model.azure.openai.autoconfigure;

import com.azure.ai.openai.OpenAIClientBuilder;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass({ AzureOpenAiEmbeddingModel.class })
@EnableConfigurationProperties({ AzureOpenAiEmbeddingProperties.class })
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.AZURE_OPENAI,
		matchIfMissing = true)
@Import(AzureOpenAiClientBuilderConfiguration.class)
public class AzureOpenAiEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AzureOpenAiEmbeddingModel azureOpenAiEmbeddingModel(OpenAIClientBuilder openAIClient,
			AzureOpenAiEmbeddingProperties embeddingProperties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {

		var embeddingModel = new AzureOpenAiEmbeddingModel(openAIClient.buildClient(),
				embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(),
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;

	}

}
