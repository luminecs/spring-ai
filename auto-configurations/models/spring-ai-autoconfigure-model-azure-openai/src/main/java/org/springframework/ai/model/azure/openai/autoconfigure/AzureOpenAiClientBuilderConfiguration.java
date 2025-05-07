package org.springframework.ai.model.azure.openai.autoconfigure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@ConditionalOnClass({ OpenAIClientBuilder.class })
@EnableConfigurationProperties(AzureOpenAiConnectionProperties.class)
public class AzureOpenAiClientBuilderConfiguration {

	private static final String APPLICATION_ID = "spring-ai";

	@Bean
	@ConditionalOnMissingBean
	public OpenAIClientBuilder openAIClientBuilder(AzureOpenAiConnectionProperties connectionProperties,
			ObjectProvider<AzureOpenAIClientBuilderCustomizer> customizers) {

		final OpenAIClientBuilder clientBuilder;

		if (StringUtils.hasText(connectionProperties.getOpenAiApiKey())) {
			clientBuilder = new OpenAIClientBuilder().endpoint("https://api.openai.com/v1")
				.credential(new KeyCredential(connectionProperties.getOpenAiApiKey()))
				.clientOptions(new ClientOptions().setApplicationId(APPLICATION_ID));
			applyOpenAIClientBuilderCustomizers(clientBuilder, customizers);
			return clientBuilder;
		}

		Map<String, String> customHeaders = connectionProperties.getCustomHeaders();
		List<Header> headers = customHeaders.entrySet()
			.stream()
			.map(entry -> new Header(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
		ClientOptions clientOptions = new ClientOptions().setApplicationId(APPLICATION_ID).setHeaders(headers);

		Assert.hasText(connectionProperties.getEndpoint(), "Endpoint must not be empty");

		if (!StringUtils.hasText(connectionProperties.getApiKey())) {
			// Entra ID configuration, as the API key is not set
			clientBuilder = new OpenAIClientBuilder().endpoint(connectionProperties.getEndpoint())
				.credential(new DefaultAzureCredentialBuilder().build())
				.clientOptions(clientOptions);
		}
		else {
			// Azure OpenAI configuration using API key and endpoint
			clientBuilder = new OpenAIClientBuilder().endpoint(connectionProperties.getEndpoint())
				.credential(new AzureKeyCredential(connectionProperties.getApiKey()))
				.clientOptions(clientOptions);
		}
		applyOpenAIClientBuilderCustomizers(clientBuilder, customizers);
		return clientBuilder;
	}

	private void applyOpenAIClientBuilderCustomizers(OpenAIClientBuilder clientBuilder,
			ObjectProvider<AzureOpenAIClientBuilderCustomizer> customizers) {
		customizers.orderedStream().forEach(customizer -> customizer.customize(clientBuilder));
	}

}
