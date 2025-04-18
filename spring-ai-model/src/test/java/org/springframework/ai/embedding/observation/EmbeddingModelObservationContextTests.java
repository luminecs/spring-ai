package org.springframework.ai.embedding.observation;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.EmbeddingRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbeddingModelObservationContextTests {

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = EmbeddingModelObservationContext.builder()
			.embeddingRequest(generateEmbeddingRequest())
			.provider("superprovider")
			.requestOptions(EmbeddingOptionsBuilder.builder().withModel("supermodel").build())
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenRequestOptionsIsNullThenThrow() {
		assertThatThrownBy(() -> EmbeddingModelObservationContext.builder()
			.embeddingRequest(generateEmbeddingRequest())
			.provider("superprovider")
			.requestOptions(null)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("requestOptions cannot be null");
	}

	private EmbeddingRequest generateEmbeddingRequest() {
		return new EmbeddingRequest(List.of(), EmbeddingOptionsBuilder.builder().build());
	}

}
