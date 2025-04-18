package org.springframework.ai.bedrock.titan.api;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.RequiresAwsCredentials;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingRequest;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingResponse;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

@RequiresAwsCredentials
public class TitanEmbeddingBedrockApiIT {

	@Test
	public void embedTextV1() {

		TitanEmbeddingBedrockApi titanEmbedApi = new TitanEmbeddingBedrockApi(
				TitanEmbeddingModel.TITAN_EMBED_TEXT_V1.id(), EnvironmentVariableCredentialsProvider.create(),
				Region.US_EAST_1.id(), new ObjectMapper(), Duration.ofMinutes(2));

		TitanEmbeddingRequest request = TitanEmbeddingRequest.builder().inputText("I like to eat apples.").build();

		TitanEmbeddingResponse response = titanEmbedApi.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.inputTextTokenCount()).isEqualTo(6);
		assertThat(response.embedding()).hasSize(1536);
	}

	@Test
	public void embedTextV2() {

		TitanEmbeddingBedrockApi titanEmbedApi = new TitanEmbeddingBedrockApi(
				TitanEmbeddingModel.TITAN_EMBED_TEXT_V2.id(), EnvironmentVariableCredentialsProvider.create(),
				Region.US_EAST_1.id(), new ObjectMapper(), Duration.ofMinutes(2));

		TitanEmbeddingRequest request = TitanEmbeddingRequest.builder().inputText("I like to eat apples.").build();

		TitanEmbeddingResponse response = titanEmbedApi.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.inputTextTokenCount()).isEqualTo(7);
		assertThat(response.embedding()).hasSize(1024);
	}

	@Test
	public void embedImage() throws IOException {

		TitanEmbeddingBedrockApi titanEmbedApi = new TitanEmbeddingBedrockApi(
				TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id(), EnvironmentVariableCredentialsProvider.create(),
				Region.US_EAST_1.id(), new ObjectMapper(), Duration.ofMinutes(2));

		byte[] image = new DefaultResourceLoader().getResource("classpath:/spring_framework.png")
			.getContentAsByteArray();

		String imageBase64 = Base64.getEncoder().encodeToString(image);
		System.out.println(imageBase64.length());

		TitanEmbeddingRequest request = TitanEmbeddingRequest.builder().inputImage(imageBase64).build();

		TitanEmbeddingResponse response = titanEmbedApi.embedding(request);

		assertThat(response).isNotNull();
		assertThat(response.inputTextTokenCount()).isEqualTo(0);
		assertThat(response.embedding()).hasSize(1024);
	}

}
