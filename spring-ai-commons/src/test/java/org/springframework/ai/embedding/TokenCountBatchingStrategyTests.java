package org.springframework.ai.embedding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenCountBatchingStrategyTests {

	@Test
	void batchEmbeddingHappyPath() {
		TokenCountBatchingStrategy tokenCountBatchingStrategy = new TokenCountBatchingStrategy();
		List<List<Document>> batch = tokenCountBatchingStrategy.batch(
				List.of(new Document("Hello world"), new Document("Hello Spring"), new Document("Hello Spring AI!")));
		assertThat(batch.size()).isEqualTo(1);
		assertThat(batch.get(0).size()).isEqualTo(3);
	}

	@Test
	void batchEmbeddingWithLargeDocumentExceedsMaxTokenSize() throws IOException {
		Resource resource = new DefaultResourceLoader().getResource("classpath:text_source.txt");
		String contentAsString = resource.getContentAsString(StandardCharsets.UTF_8);
		TokenCountBatchingStrategy tokenCountBatchingStrategy = new TokenCountBatchingStrategy();
		assertThatThrownBy(() -> tokenCountBatchingStrategy.batch(List.of(new Document(contentAsString))))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
