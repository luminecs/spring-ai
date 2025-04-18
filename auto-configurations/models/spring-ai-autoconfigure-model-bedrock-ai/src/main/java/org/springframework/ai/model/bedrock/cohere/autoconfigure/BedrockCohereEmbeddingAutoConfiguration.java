package org.springframework.ai.model.bedrock.cohere.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionConfiguration;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(CohereEmbeddingBedrockApi.class)
@EnableConfigurationProperties({ BedrockCohereEmbeddingProperties.class, BedrockAwsConnectionProperties.class })
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.BEDROCK_COHERE,
		matchIfMissing = true)
@Import(BedrockAwsConnectionConfiguration.class)
public class BedrockCohereEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({ AwsCredentialsProvider.class, AwsRegionProvider.class })
	public CohereEmbeddingBedrockApi cohereEmbeddingApi(AwsCredentialsProvider credentialsProvider,
			AwsRegionProvider regionProvider, BedrockCohereEmbeddingProperties properties,
			BedrockAwsConnectionProperties awsProperties, ObjectMapper objectMapper) {
		return new CohereEmbeddingBedrockApi(properties.getModel(), credentialsProvider, regionProvider.getRegion(),
				objectMapper, awsProperties.getTimeout());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(CohereEmbeddingBedrockApi.class)
	public BedrockCohereEmbeddingModel cohereEmbeddingModel(CohereEmbeddingBedrockApi cohereEmbeddingApi,
			BedrockCohereEmbeddingProperties properties) {

		return new BedrockCohereEmbeddingModel(cohereEmbeddingApi, properties.getOptions());
	}

}
