package org.springframework.ai.oci;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = org.springframework.ai.oci.BaseEmbeddingModelTest.OCI_COMPARTMENT_ID_KEY,
		matches = ".+")
class OCIEmbeddingModelIT extends BaseEmbeddingModelTest {

	private final OCIEmbeddingModel embeddingModel = getEmbeddingModel();

	private final List<String> content = List.of("How many states are in the USA?", "How many states are in India?");

	@Test
	void embed() {
		float[] embedding = this.embeddingModel.embed(new Document("How many provinces are in Canada?"));
		assertThat(embedding).hasSize(1024);
	}

	@Test
	void call() {
		EmbeddingResponse response = this.embeddingModel.call(new EmbeddingRequest(this.content, null));
		assertThat(response).isNotNull();
		assertThat(response.getResults()).hasSize(2);
		assertThat(response.getMetadata().getModel()).isEqualTo(EMBEDDING_MODEL_V2);
	}

	@Test
	void callWithOptions() {
		EmbeddingResponse response = this.embeddingModel
			.call(new EmbeddingRequest(this.content, OCIEmbeddingOptions.builder().model(EMBEDDING_MODEL_V3).build()));
		assertThat(response).isNotNull();
		assertThat(response.getResults()).hasSize(2);
		assertThat(response.getMetadata().getModel()).isEqualTo(EMBEDDING_MODEL_V3);
	}

}
