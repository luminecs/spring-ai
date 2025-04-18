package org.springframework.ai.vectorstore.qdrant.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;

import static org.assertj.core.api.Assertions.assertThat;

public class QdrantVectorStorePropertiesTests {

	@Test
	public void defaultValues() {
		var props = new QdrantVectorStoreProperties();

		assertThat(props.getCollectionName()).isEqualTo(QdrantVectorStore.DEFAULT_COLLECTION_NAME);
		assertThat(props.getHost()).isEqualTo("localhost");
		assertThat(props.getPort()).isEqualTo(6334);
		assertThat(props.isUseTls()).isFalse();
		assertThat(props.getApiKey()).isNull();
	}

	@Test
	public void customValues() {
		var props = new QdrantVectorStoreProperties();

		props.setCollectionName("MY_COLLECTION");
		props.setHost("MY_HOST");
		props.setPort(999);
		props.setUseTls(true);
		props.setApiKey("MY_API_KEY");

		assertThat(props.getCollectionName()).isEqualTo("MY_COLLECTION");
		assertThat(props.getHost()).isEqualTo("MY_HOST");
		assertThat(props.getPort()).isEqualTo(999);
		assertThat(props.isUseTls()).isTrue();
		assertThat(props.getApiKey()).isEqualTo("MY_API_KEY");
	}

}
