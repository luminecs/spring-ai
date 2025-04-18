package org.springframework.ai.model.azure.openai.autoconfigure;

import com.azure.ai.openai.OpenAIClientBuilder;

@FunctionalInterface
public interface AzureOpenAIClientBuilderCustomizer {

	void customize(OpenAIClientBuilder clientBuilder);

}
