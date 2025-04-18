package org.springframework.ai.bedrock.titan;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.RequiresAwsCredentials;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel.InputType;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiresAwsCredentials
class BedrockTitanEmbeddingModelIT {

	@Autowired
	private BedrockTitanEmbeddingModel embeddingModel;

	@Test
	void singleEmbedding() {
		assertThat(this.embeddingModel).isNotNull();
		EmbeddingResponse embeddingResponse = this.embeddingModel.call(new EmbeddingRequest(List.of("Hello World"),
				BedrockTitanEmbeddingOptions.builder().withInputType(InputType.TEXT).build()));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		assertThat(embeddingResponse.getResults().get(0).getOutput()).isNotEmpty();
		assertThat(this.embeddingModel.dimensions()).isEqualTo(1024);
	}

	@Test
	void imageEmbedding() throws IOException {

		byte[] image = new DefaultResourceLoader().getResource("classpath:/spring_framework.png")
			.getContentAsByteArray();

		EmbeddingResponse embeddingResponse = this.embeddingModel
			.call(new EmbeddingRequest(List.of(Base64.getEncoder().encodeToString(image)),
					BedrockTitanEmbeddingOptions.builder().withInputType(InputType.IMAGE).build()));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		assertThat(embeddingResponse.getResults().get(0).getOutput()).isNotEmpty();
		assertThat(this.embeddingModel.dimensions()).isEqualTo(1024);
	}

	@SpringBootConfiguration
	public static class TestConfiguration {

		@Bean
		public TitanEmbeddingBedrockApi titanEmbeddingApi() {
			return new TitanEmbeddingBedrockApi(TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id(),
					EnvironmentVariableCredentialsProvider.create(), Region.US_EAST_1.id(), new ObjectMapper(),
					Duration.ofMinutes(2));
		}

		@Bean
		public BedrockTitanEmbeddingModel titanEmbedding(TitanEmbeddingBedrockApi titanEmbeddingApi) {
			return new BedrockTitanEmbeddingModel(titanEmbeddingApi);
		}

	}

}
