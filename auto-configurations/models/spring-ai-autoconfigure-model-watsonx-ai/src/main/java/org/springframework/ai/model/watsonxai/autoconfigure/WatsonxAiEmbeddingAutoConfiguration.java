package org.springframework.ai.model.watsonxai.autoconfigure;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.watsonx.WatsonxAiEmbeddingModel;
import org.springframework.ai.watsonx.api.WatsonxAiApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration(after = RestClientAutoConfiguration.class)
@ConditionalOnClass(WatsonxAiApi.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.WATSONX_AI,
		matchIfMissing = true)
@EnableConfigurationProperties({ WatsonxAiConnectionProperties.class, WatsonxAiEmbeddingProperties.class })
public class WatsonxAiEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WatsonxAiApi watsonxApi(WatsonxAiConnectionProperties properties,
			ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
		return new WatsonxAiApi(properties.getBaseUrl(), properties.getStreamEndpoint(), properties.getTextEndpoint(),
				properties.getEmbeddingEndpoint(), properties.getProjectId(), properties.getIAMToken(),
				restClientBuilderProvider.getIfAvailable(RestClient::builder));
	}

	@Bean
	@ConditionalOnMissingBean
	public WatsonxAiEmbeddingModel watsonxAiEmbeddingModel(WatsonxAiApi watsonxApi,
			WatsonxAiEmbeddingProperties properties) {
		return new WatsonxAiEmbeddingModel(watsonxApi, properties.getOptions());
	}

}
