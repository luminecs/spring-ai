package org.springframework.ai.postgresml;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresMlEmbeddingOptionsTests {

	@Test
	public void defaultOptions() {
		PostgresMlEmbeddingOptions options = PostgresMlEmbeddingOptions.builder().build();

		assertThat(options.getTransformer()).isEqualTo(PostgresMlEmbeddingModel.DEFAULT_TRANSFORMER_MODEL);
		assertThat(options.getVectorType()).isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_ARRAY);
		assertThat(options.getKwargs()).isEqualTo(Map.of());
		assertThat(options.getMetadataMode()).isEqualTo(org.springframework.ai.document.MetadataMode.EMBED);
	}

	@Test
	public void newOptions() {
		PostgresMlEmbeddingOptions options = PostgresMlEmbeddingOptions.builder()
			.transformer("intfloat/e5-small")
			.vectorType(PostgresMlEmbeddingModel.VectorType.PG_VECTOR)
			.metadataMode(org.springframework.ai.document.MetadataMode.ALL)
			.kwargs(Map.of("device", "cpu"))
			.build();

		assertThat(options.getTransformer()).isEqualTo("intfloat/e5-small");
		assertThat(options.getVectorType()).isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_VECTOR);
		assertThat(options.getKwargs()).isEqualTo(Map.of("device", "cpu"));
		assertThat(options.getMetadataMode()).isEqualTo(org.springframework.ai.document.MetadataMode.ALL);
	}

	@Test
	public void mergeOptions() {

		var jdbcTemplate = Mockito.mock(JdbcTemplate.class);
		PostgresMlEmbeddingModel embeddingModel = new PostgresMlEmbeddingModel(jdbcTemplate);

		PostgresMlEmbeddingOptions options = embeddingModel.mergeOptions(EmbeddingOptionsBuilder.builder().build());

		assertThat(options.getTransformer()).isEqualTo(PostgresMlEmbeddingModel.DEFAULT_TRANSFORMER_MODEL);
		assertThat(options.getVectorType()).isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_ARRAY);
		assertThat(options.getKwargs()).isEqualTo(Map.of());
		assertThat(options.getMetadataMode()).isEqualTo(org.springframework.ai.document.MetadataMode.EMBED);

		options = embeddingModel.mergeOptions(PostgresMlEmbeddingOptions.builder()
			.transformer("intfloat/e5-small")
			.kwargs(Map.of("device", "cpu"))
			.build());

		assertThat(options.getTransformer()).isEqualTo("intfloat/e5-small");
		assertThat(options.getVectorType()).isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_ARRAY);
		assertThat(options.getKwargs()).isEqualTo(Map.of("device", "cpu"));
		assertThat(options.getMetadataMode()).isEqualTo(org.springframework.ai.document.MetadataMode.EMBED);

		options = embeddingModel.mergeOptions(PostgresMlEmbeddingOptions.builder()
			.transformer("intfloat/e5-small")
			.vectorType(PostgresMlEmbeddingModel.VectorType.PG_VECTOR)
			.metadataMode(org.springframework.ai.document.MetadataMode.ALL)
			.kwargs(Map.of("device", "cpu"))
			.build());

		assertThat(options.getTransformer()).isEqualTo("intfloat/e5-small");
		assertThat(options.getVectorType()).isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_VECTOR);
		assertThat(options.getKwargs()).isEqualTo(Map.of("device", "cpu"));
		assertThat(options.getMetadataMode()).isEqualTo(org.springframework.ai.document.MetadataMode.ALL);
	}

}
