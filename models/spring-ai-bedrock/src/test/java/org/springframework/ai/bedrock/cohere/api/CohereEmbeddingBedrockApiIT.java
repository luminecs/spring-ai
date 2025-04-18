package org.springframework.ai.bedrock.cohere.api;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.RequiresAwsCredentials;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingResponse;

import static org.assertj.core.api.Assertions.assertThat;

@RequiresAwsCredentials
public class CohereEmbeddingBedrockApiIT {

	CohereEmbeddingBedrockApi api = new CohereEmbeddingBedrockApi(
			CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3.id(), EnvironmentVariableCredentialsProvider.create(),
			Region.US_EAST_1.id(), new ObjectMapper(), Duration.ofMinutes(2));

	@Test
	public void embedText() {

		CohereEmbeddingRequest request = new CohereEmbeddingRequest(
				List.of("I like to eat apples", "I like to eat oranges"),
				CohereEmbeddingRequest.InputType.SEARCH_DOCUMENT, CohereEmbeddingRequest.Truncate.NONE);

		CohereEmbeddingResponse response = this.api.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.texts()).isEqualTo(request.texts());
		assertThat(response.embeddings()).hasSize(2);
		assertThat(response.embeddings().get(0)).hasSize(1024);
	}

	@Test
	public void embedTextWithTruncate() {

		CohereEmbeddingRequest request = new CohereEmbeddingRequest(
				List.of("I like to eat apples", "I like to eat oranges"),
				CohereEmbeddingRequest.InputType.SEARCH_DOCUMENT, CohereEmbeddingRequest.Truncate.START);

		CohereEmbeddingResponse response = this.api.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.texts()).isEqualTo(request.texts());
		assertThat(response.embeddings()).hasSize(2);
		assertThat(response.embeddings().get(0)).hasSize(1024);

		request = new CohereEmbeddingRequest(List.of("I like to eat apples", "I like to eat oranges"),
				CohereEmbeddingRequest.InputType.SEARCH_DOCUMENT, CohereEmbeddingRequest.Truncate.END);

		response = this.api.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.texts()).isEqualTo(request.texts());
		assertThat(response.embeddings()).hasSize(2);
		assertThat(response.embeddings().get(0)).hasSize(1024);
	}

}
