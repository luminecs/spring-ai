package org.springframework.ai.model.qianfan.autoconfigure;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
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
@ConditionalOnClass(QianFanApi.class)
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.QIANFAN,
		matchIfMissing = true)
@EnableConfigurationProperties({ QianFanConnectionProperties.class, QianFanImageProperties.class })
public class QianFanImageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public QianFanImageModel qianFanImageModel(QianFanConnectionProperties commonProperties,
			QianFanImageProperties imageProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider,
			RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<ImageModelObservationConvention> observationConvention) {

		String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey()
				: commonProperties.getApiKey();

		String secretKey = StringUtils.hasText(imageProperties.getSecretKey()) ? imageProperties.getSecretKey()
				: commonProperties.getSecretKey();

		String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl()
				: commonProperties.getBaseUrl();

		Assert.hasText(apiKey, "QianFan API key must be set.  Use the property: spring.ai.qianfan.api-key");
		Assert.hasText(secretKey, "QianFan secret key must be set.  Use the property: spring.ai.qianfan.secret-key");
		Assert.hasText(baseUrl, "QianFan base URL must be set.  Use the property: spring.ai.qianfan.base-url");

		var qianFanImageApi = new QianFanImageApi(baseUrl, apiKey, secretKey,
				restClientBuilderProvider.getIfAvailable(RestClient::builder), responseErrorHandler);

		var imageModel = new QianFanImageModel(qianFanImageApi, imageProperties.getOptions(), retryTemplate,
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		observationConvention.ifAvailable(imageModel::setObservationConvention);

		return imageModel;
	}

}
