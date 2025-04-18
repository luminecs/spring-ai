package org.springframework.ai.model.stabilityai.autoconfigure;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@AutoConfiguration(after = { RestClientAutoConfiguration.class })
@ConditionalOnClass(StabilityAiApi.class)
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.STABILITY_AI,
		matchIfMissing = true)
@EnableConfigurationProperties({ StabilityAiConnectionProperties.class, StabilityAiImageProperties.class })
public class StabilityAiImageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public StabilityAiApi stabilityAiApi(StabilityAiConnectionProperties commonProperties,
			StabilityAiImageProperties imageProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider) {

		String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey()
				: commonProperties.getApiKey();

		String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl()
				: commonProperties.getBaseUrl();

		Assert.hasText(apiKey, "StabilityAI API key must be set");
		Assert.hasText(baseUrl, "StabilityAI base URL must be set");

		return new StabilityAiApi(apiKey, imageProperties.getOptions().getModel(), baseUrl,
				restClientBuilderProvider.getIfAvailable(RestClient::builder));
	}

	@Bean
	@ConditionalOnMissingBean
	public StabilityAiImageModel stabilityAiImageModel(StabilityAiApi stabilityAiApi,
			StabilityAiImageProperties stabilityAiImageProperties) {
		return new StabilityAiImageModel(stabilityAiApi, stabilityAiImageProperties.getOptions());
	}

}
