package org.springframework.ai.mistralai;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MistralAiTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".+")
class MistralAiEmbeddingIT {

	@Autowired
	private MistralAiEmbeddingModel mistralAiEmbeddingModel;

	@Test
	void defaultEmbedding() {
		assertThat(this.mistralAiEmbeddingModel).isNotNull();
		var embeddingResponse = this.mistralAiEmbeddingModel.embedForResponse(List.of("Hello World"));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		assertThat(embeddingResponse.getResults().get(0)).isNotNull();
		assertThat(embeddingResponse.getResults().get(0).getOutput()).hasSize(1024);
		assertThat(embeddingResponse.getMetadata().getModel()).isEqualTo("mistral-embed");
		assertThat(embeddingResponse.getMetadata().getUsage().getTotalTokens()).isEqualTo(4);
		assertThat(embeddingResponse.getMetadata().getUsage().getPromptTokens()).isEqualTo(4);
		assertThat(this.mistralAiEmbeddingModel.dimensions()).isEqualTo(1024);
	}

	@Test
	void embeddingTest() {
		assertThat(this.mistralAiEmbeddingModel).isNotNull();
		var embeddingResponse = this.mistralAiEmbeddingModel.call(new EmbeddingRequest(
				List.of("Hello World", "World is big"),
				MistralAiEmbeddingOptions.builder().withModel("mistral-embed").withEncodingFormat("float").build()));
		assertThat(embeddingResponse.getResults()).hasSize(2);
		assertThat(embeddingResponse.getResults().get(0)).isNotNull();
		assertThat(embeddingResponse.getResults().get(0).getOutput()).hasSize(1024);
		assertThat(embeddingResponse.getMetadata().getModel()).isEqualTo("mistral-embed");
		assertThat(embeddingResponse.getMetadata().getUsage().getTotalTokens()).isEqualTo(9);
		assertThat(embeddingResponse.getMetadata().getUsage().getPromptTokens()).isEqualTo(9);
		assertThat(this.mistralAiEmbeddingModel.dimensions()).isEqualTo(1024);
	}

}
