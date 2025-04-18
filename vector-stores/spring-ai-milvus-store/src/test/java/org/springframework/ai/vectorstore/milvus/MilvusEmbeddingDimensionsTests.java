package org.springframework.ai.vectorstore.milvus;

import io.milvus.client.MilvusServiceClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MilvusEmbeddingDimensionsTests {

	@Mock
	private EmbeddingModel embeddingModel;

	@Mock
	private MilvusServiceClient milvusClient;

	@Test
	public void explicitlySetDimensions() {

		final int explicitDimensions = 696;

		MilvusVectorStore build = MilvusVectorStore.builder(this.milvusClient, this.embeddingModel)
			.initializeSchema(true)
			.batchingStrategy(new TokenCountBatchingStrategy())
			.embeddingDimension(explicitDimensions)
			.build();
		var dim = build.embeddingDimensions();

		assertThat(dim).isEqualTo(explicitDimensions);
		verify(this.embeddingModel, never()).dimensions();
	}

	@Test
	public void embeddingModelDimensions() {
		given(this.embeddingModel.dimensions()).willReturn(969);

		MilvusVectorStore build = MilvusVectorStore.builder(this.milvusClient, this.embeddingModel)
			.initializeSchema(true)
			.batchingStrategy(new TokenCountBatchingStrategy())
			.build();
		var dim = build.embeddingDimensions();

		assertThat(dim).isEqualTo(969);

		verify(this.embeddingModel, only()).dimensions();
	}

	@Test
	public void fallBackToDefaultDimensions() {

		given(this.embeddingModel.dimensions()).willThrow(new RuntimeException());

		MilvusVectorStore build = MilvusVectorStore.builder(this.milvusClient, this.embeddingModel)
			.initializeSchema(true)
			.batchingStrategy(new TokenCountBatchingStrategy())
			.build();
		var dim = build.embeddingDimensions();

		assertThat(dim).isEqualTo(MilvusVectorStore.OPENAI_EMBEDDING_DIMENSION_SIZE);
		verify(this.embeddingModel, only()).dimensions();
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 32769 })
	public void invalidDimensionsThrowException(final int explicitDimensions) {

		ThrowableAssert.ThrowingCallable actual = () -> MilvusVectorStore
			.builder(this.milvusClient, this.embeddingModel)
			.embeddingDimension(explicitDimensions)
			.build();

		assertThatThrownBy(actual).isInstanceOf(IllegalArgumentException.class);
	}

}
