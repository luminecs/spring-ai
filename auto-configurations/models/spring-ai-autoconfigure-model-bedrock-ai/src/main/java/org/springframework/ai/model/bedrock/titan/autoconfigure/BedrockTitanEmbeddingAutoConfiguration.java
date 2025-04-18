package org.springframework.ai.model.bedrock.titan.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
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
@ConditionalOnClass(TitanEmbeddingBedrockApi.class)
@EnableConfigurationProperties({ BedrockTitanEmbeddingProperties.class, BedrockAwsConnectionProperties.class })
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.BEDROCK_TITAN,
		matchIfMissing = true)
@Import(BedrockAwsConnectionConfiguration.class)
public class BedrockTitanEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({ AwsCredentialsProvider.class, AwsRegionProvider.class })
	public TitanEmbeddingBedrockApi titanEmbeddingBedrockApi(AwsCredentialsProvider credentialsProvider,
			AwsRegionProvider regionProvider, BedrockTitanEmbeddingProperties properties,
			BedrockAwsConnectionProperties awsProperties, ObjectMapper objectMapper) {
		return new TitanEmbeddingBedrockApi(properties.getModel(), credentialsProvider, regionProvider.getRegion(),
				objectMapper, awsProperties.getTimeout());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(TitanEmbeddingBedrockApi.class)
	public BedrockTitanEmbeddingModel titanEmbeddingModel(TitanEmbeddingBedrockApi titanEmbeddingApi,
			BedrockTitanEmbeddingProperties properties) {
		return new BedrockTitanEmbeddingModel(titanEmbeddingApi).withInputType(properties.getInputType());
	}

}
