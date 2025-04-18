package org.springframework.ai.transformers;

import java.text.DecimalFormat;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformersEmbeddingModelTests {

	private static DecimalFormat DF = new DecimalFormat("#.#####");

	@Test
	void embed() throws Exception {

		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
		embeddingModel.afterPropertiesSet();
		float[] embed = embeddingModel.embed("Hello world");
		assertThat(embed).hasSize(384);
		assertThat(DF.format(embed[0])).isEqualTo(DF.format(-0.19744634628295898));
		assertThat(DF.format(embed[383])).isEqualTo(DF.format(0.17298996448516846));
	}

	@Test
	void embedDocument() throws Exception {
		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
		embeddingModel.afterPropertiesSet();
		float[] embed = embeddingModel.embed(new Document("Hello world"));
		assertThat(embed).hasSize(384);
		assertThat(DF.format(embed[0])).isEqualTo(DF.format(-0.19744634628295898));
		assertThat(DF.format(embed[383])).isEqualTo(DF.format(0.17298996448516846));
	}

	@Test
	void embedList() throws Exception {
		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
		embeddingModel.afterPropertiesSet();
		List<float[]> embed = embeddingModel.embed(List.of("Hello world", "World is big"));
		assertThat(embed).hasSize(2);
		assertThat(embed.get(0)).hasSize(384);
		assertThat(DF.format(embed.get(0)[0])).isEqualTo(DF.format(-0.19744634628295898));
		assertThat(DF.format(embed.get(0)[383])).isEqualTo(DF.format(0.17298996448516846));

		assertThat(embed.get(1)).hasSize(384);
		assertThat(DF.format(embed.get(1)[0])).isEqualTo(DF.format(0.4293745160102844));
		assertThat(DF.format(embed.get(1)[383])).isEqualTo(DF.format(0.05501303821802139));

		assertThat(embed.get(0)).isNotEqualTo(embed.get(1));
	}

	@Test
	void embedForResponse() throws Exception {
		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
		embeddingModel.afterPropertiesSet();
		EmbeddingResponse embed = embeddingModel.embedForResponse(List.of("Hello world", "World is big"));
		assertThat(embed.getResults()).hasSize(2);
		assertTrue(embed.getMetadata().isEmpty(), "Expected embed metadata to be empty, but it was not.");

		assertThat(embed.getResults().get(0).getOutput()).hasSize(384);
		assertThat(DF.format(embed.getResults().get(0).getOutput()[0])).isEqualTo(DF.format(-0.19744634628295898));
		assertThat(DF.format(embed.getResults().get(0).getOutput()[383])).isEqualTo(DF.format(0.17298996448516846));

		assertThat(embed.getResults().get(1).getOutput()).hasSize(384);
		assertThat(DF.format(embed.getResults().get(1).getOutput()[0])).isEqualTo(DF.format(0.4293745160102844));
		assertThat(DF.format(embed.getResults().get(1).getOutput()[383])).isEqualTo(DF.format(0.05501303821802139));
	}

	@Test
	void dimensions() throws Exception {

		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
		embeddingModel.afterPropertiesSet();
		assertThat(embeddingModel.dimensions()).isEqualTo(384);

		assertThat(embeddingModel.dimensions()).isEqualTo(384);
	}

}
