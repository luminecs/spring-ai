package org.springframework.ai.vectorstore.typesense;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.resources.Node;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TypesenseVectorStoreBuilderTests {

	private final Client client;

	private final EmbeddingModel embeddingModel;

	TypesenseVectorStoreBuilderTests() {
		List<Node> nodes = new ArrayList<>();
		nodes.add(new Node("http", "localhost", "8108"));
		this.client = new Client(new Configuration(nodes, Duration.ofSeconds(5), "xyz"));
		this.embeddingModel = mock(EmbeddingModel.class);
	}

	@Test
	void defaultConfiguration() {
		TypesenseVectorStore vectorStore = TypesenseVectorStore.builder(this.client, this.embeddingModel).build();

		assertThat(vectorStore).hasFieldOrPropertyWithValue("collectionName", "vector_store");
		assertThat(vectorStore).hasFieldOrPropertyWithValue("embeddingDimension", -1);
		assertThat(vectorStore).hasFieldOrPropertyWithValue("initializeSchema", false);
		assertThat(vectorStore).hasFieldOrPropertyWithValue("batchingStrategy.class", TokenCountBatchingStrategy.class);
	}

	@Test
	void customConfiguration() {
		TypesenseVectorStore vectorStore = TypesenseVectorStore.builder(this.client, this.embeddingModel)
			.collectionName("custom_collection")
			.embeddingDimension(1536)
			.initializeSchema(true)
			.build();

		assertThat(vectorStore).hasFieldOrPropertyWithValue("collectionName", "custom_collection");
		assertThat(vectorStore).hasFieldOrPropertyWithValue("embeddingDimension", 1536);
		assertThat(vectorStore).hasFieldOrPropertyWithValue("initializeSchema", true);
	}

	@Test
	void nullClientShouldThrowException() {
		assertThatThrownBy(() -> TypesenseVectorStore.builder(null, this.embeddingModel).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("client must not be null");
	}

	@Test
	void nullEmbeddingModelShouldThrowException() {
		assertThatThrownBy(() -> TypesenseVectorStore.builder(this.client, null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("EmbeddingModel must be configured");
	}

	@Test
	void invalidEmbeddingDimensionShouldThrowException() {
		assertThatThrownBy(
				() -> TypesenseVectorStore.builder(this.client, this.embeddingModel).embeddingDimension(0).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Embedding dimension must be greater than 0");
	}

	@Test
	void emptyCollectionNameShouldThrowException() {
		assertThatThrownBy(
				() -> TypesenseVectorStore.builder(this.client, this.embeddingModel).collectionName("").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("collectionName must not be empty");
	}

	@Test
	void nullBatchingStrategyShouldThrowException() {
		assertThatThrownBy(
				() -> TypesenseVectorStore.builder(this.client, this.embeddingModel).batchingStrategy(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("BatchingStrategy must not be null");
	}

}
