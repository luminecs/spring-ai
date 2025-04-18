package org.springframework.ai.model.azure.openai.autoconfigure;

import com.azure.ai.openai.OpenAIClientBuilder;

import org.springframework.ai.azure.openai.AzureOpenAiImageModel;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(AzureOpenAiImageModel.class)
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.AZURE_OPENAI,
		matchIfMissing = true)
@EnableConfigurationProperties(AzureOpenAiImageOptionsProperties.class)
@Import(AzureOpenAiClientBuilderConfiguration.class)
public class AzureOpenAiImageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AzureOpenAiImageModel azureOpenAiImageModel(OpenAIClientBuilder openAIClientBuilder,
			AzureOpenAiImageOptionsProperties imageProperties) {

		return new AzureOpenAiImageModel(openAIClientBuilder.buildClient(), imageProperties.getOptions());
	}

}
