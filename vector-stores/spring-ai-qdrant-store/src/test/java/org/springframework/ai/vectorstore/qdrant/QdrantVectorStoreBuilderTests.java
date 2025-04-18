package org.springframework.ai.vectorstore.qdrant;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class QdrantVectorStoreBuilderTests {

	private QdrantClient qdrantClient;

	private EmbeddingModel embeddingModel;

	@BeforeEach
	void setUp() {
		this.qdrantClient = mock(QdrantClient.class);
		this.embeddingModel = mock(EmbeddingModel.class);
	}

	@Test
	void defaultConfiguration() {
		QdrantVectorStore vectorStore = QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel).build();

		assertThat(vectorStore).hasFieldOrPropertyWithValue("collectionName", "vector_store");
		assertThat(vectorStore).hasFieldOrPropertyWithValue("initializeSchema", false);
		assertThat(vectorStore).hasFieldOrPropertyWithValue("batchingStrategy.class", TokenCountBatchingStrategy.class);
	}

	@Test
	void customConfiguration() {
		QdrantVectorStore vectorStore = QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel)
			.collectionName("custom_collection")
			.initializeSchema(true)
			.batchingStrategy(new TokenCountBatchingStrategy())
			.build();

		assertThat(vectorStore).hasFieldOrPropertyWithValue("collectionName", "custom_collection");
		assertThat(vectorStore).hasFieldOrPropertyWithValue("initializeSchema", true);
		assertThat(vectorStore).hasFieldOrPropertyWithValue("batchingStrategy.class", TokenCountBatchingStrategy.class);
	}

	@Test
	void nullQdrantClientInConstructorShouldThrowException() {
		assertThatThrownBy(() -> QdrantVectorStore.builder(null, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("EmbeddingModel must be configured");
	}

	@Test
	void nullEmbeddingModelShouldThrowException() {
		assertThatThrownBy(() -> QdrantVectorStore.builder(this.qdrantClient, null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("EmbeddingModel must be configured");
	}

	@Test
	void emptyCollectionNameShouldThrowException() {
		assertThatThrownBy(
				() -> QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel).collectionName("").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("collectionName must not be empty");
	}

	@Test
	void nullBatchingStrategyShouldThrowException() {
		assertThatThrownBy(
				() -> QdrantVectorStore.builder(this.qdrantClient, this.embeddingModel).batchingStrategy(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("BatchingStrategy must not be null");
	}

}
