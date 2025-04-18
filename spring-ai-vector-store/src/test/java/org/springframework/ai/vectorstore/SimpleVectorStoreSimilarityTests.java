package org.springframework.ai.vectorstore;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleVectorStoreSimilarityTests {

	@Test
	public void testSimilarity() {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("foo", "bar");
		float[] testEmbedding = new float[] { 1.0f, 2.0f, 3.0f };

		SimpleVectorStoreContent storeContent = new SimpleVectorStoreContent("1", "hello, how are you?", metadata,
				testEmbedding);
		Document document = storeContent.toDocument(0.6);
		assertThat(document).isNotNull();
		assertThat(document.getId()).isEqualTo("1");
		assertThat(document.getText()).isEqualTo("hello, how are you?");
		assertThat(document.getMetadata().get("foo")).isEqualTo("bar");
	}

}
