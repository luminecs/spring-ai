package org.springframework.ai.azure.openai;

import com.azure.ai.openai.OpenAIClientBuilder;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootConfiguration
@Profile("spring-ai-azure-openai-mocks")
@Import(MockAiTestConfiguration.class)
@SuppressWarnings("unused")
public class MockAzureOpenAiTestConfiguration {

	@Bean
	OpenAIClientBuilder microsoftAzureOpenAiClient(MockWebServer webServer) {

		HttpUrl baseUrl = webServer.url(MockAiTestConfiguration.SPRING_AI_API_PATH);

		return new OpenAIClientBuilder().endpoint(baseUrl.toString());
	}

	@Bean
	AzureOpenAiChatModel azureOpenAiChatModel(OpenAIClientBuilder microsoftAzureOpenAiClient) {
		return AzureOpenAiChatModel.builder().openAIClientBuilder(microsoftAzureOpenAiClient).build();
	}

}
