package org.springframework.ai.embedding.observation;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.EmbeddingRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbeddingModelObservationContextTests {

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = EmbeddingModelObservationContext.builder()
			.embeddingRequest(
					generateEmbeddingRequest(EmbeddingOptionsBuilder.builder().withModel("supermodel").build()))
			.provider("superprovider")
			.build();

		assertThat(observationContext).isNotNull();
	}

	private EmbeddingRequest generateEmbeddingRequest(EmbeddingOptions embeddingOptions) {
		return new EmbeddingRequest(List.of(), embeddingOptions);
	}

}
