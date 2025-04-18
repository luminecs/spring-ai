package org.springframework.ai.azure.openai;

import java.util.List;

import com.azure.ai.openai.OpenAIClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureEmbeddingsOptionsTests {

	@Test
	public void createRequestWithChatOptions() {

		OpenAIClient mockClient = Mockito.mock(OpenAIClient.class);
		var client = new AzureOpenAiEmbeddingModel(mockClient, MetadataMode.EMBED,
				AzureOpenAiEmbeddingOptions.builder().deploymentName("DEFAULT_MODEL").user("USER_TEST").build());

		var requestOptions = client.toEmbeddingOptions(new EmbeddingRequest(List.of("Test message content"), null));

		assertThat(requestOptions.getInput()).hasSize(1);

		assertThat(requestOptions.getModel()).isEqualTo("DEFAULT_MODEL");
		assertThat(requestOptions.getUser()).isEqualTo("USER_TEST");

		requestOptions = client.toEmbeddingOptions(new EmbeddingRequest(List.of("Test message content"),
				AzureOpenAiEmbeddingOptions.builder().deploymentName("PROMPT_MODEL").user("PROMPT_USER").build()));

		assertThat(requestOptions.getInput()).hasSize(1);

		assertThat(requestOptions.getModel()).isEqualTo("PROMPT_MODEL");
		assertThat(requestOptions.getUser()).isEqualTo("PROMPT_USER");
	}

}
