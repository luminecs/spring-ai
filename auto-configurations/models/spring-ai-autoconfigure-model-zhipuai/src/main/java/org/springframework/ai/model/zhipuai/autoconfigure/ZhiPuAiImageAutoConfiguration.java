package org.springframework.ai.model.zhipuai.autoconfigure;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@ConditionalOnClass(ZhiPuAiApi.class)
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.ZHIPUAI,
		matchIfMissing = true)
@EnableConfigurationProperties({ ZhiPuAiConnectionProperties.class, ZhiPuAiImageProperties.class })
public class ZhiPuAiImageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ZhiPuAiImageModel zhiPuAiImageModel(ZhiPuAiConnectionProperties commonProperties,
			ZhiPuAiImageProperties imageProperties, RestClient.Builder restClientBuilder, RetryTemplate retryTemplate,
			ResponseErrorHandler responseErrorHandler) {

		String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey()
				: commonProperties.getApiKey();

		String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl()
				: commonProperties.getBaseUrl();

		Assert.hasText(apiKey, "ZhiPuAI API key must be set");
		Assert.hasText(baseUrl, "ZhiPuAI base URL must be set");

		var zhiPuAiImageApi = new ZhiPuAiImageApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);

		return new ZhiPuAiImageModel(zhiPuAiImageApi, imageProperties.getOptions(), retryTemplate);
	}

}
