package org.springframework.ai.model.postgresml.autoconfigure;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.postgresml.PostgresMlEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = { "spring.ai.postgresml.embedding.options.metadata-mode=all",
		"spring.ai.postgresml.embedding.options.kwargs.key1=value1",
		"spring.ai.postgresml.embedding.options.kwargs.key2=value2",
		"spring.ai.postgresml.embedding.options.transformer=abc123" })
class PostgresMlEmbeddingPropertiesTests {

	@Autowired
	private PostgresMlEmbeddingProperties postgresMlProperties;

	@Test
	void postgresMlPropertiesAreCorrect() {
		assertThat(this.postgresMlProperties).isNotNull();
		assertThat(this.postgresMlProperties.getOptions().getTransformer()).isEqualTo("abc123");
		assertThat(this.postgresMlProperties.getOptions().getVectorType())
			.isEqualTo(PostgresMlEmbeddingModel.VectorType.PG_ARRAY);
		assertThat(this.postgresMlProperties.getOptions().getKwargs())
			.isEqualTo(Map.of("key1", "value1", "key2", "value2"));
		assertThat(this.postgresMlProperties.getOptions().getMetadataMode()).isEqualTo(MetadataMode.ALL);
	}

	@SpringBootConfiguration
	@EnableConfigurationProperties(PostgresMlEmbeddingProperties.class)
	static class TestConfiguration {

	}

}
