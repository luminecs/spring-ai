package org.springframework.ai.embedding;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AbstractEmbeddingModelTests {

	@Mock
	private EmbeddingModel embeddingModel;

	@Test
	public void testDefaultMethodImplementation() {

		EmbeddingModel dummy = new EmbeddingModel() {

			@Override
			public float[] embed(String text) {
				return new float[] { 0.1f, 0.1f, 0.1f };
			}

			@Override
			public float[] embed(Document document) {
				throw new UnsupportedOperationException("Unimplemented method 'embed'");
			}

			@Override
			public List<float[]> embed(List<String> texts) {
				throw new UnsupportedOperationException("Unimplemented method 'embed'");
			}

			@Override
			public EmbeddingResponse embedForResponse(List<String> texts) {
				throw new UnsupportedOperationException("Unimplemented method 'embedForResponse'");
			}

			@Override
			public EmbeddingResponse call(EmbeddingRequest request) {
				throw new UnsupportedOperationException("Unimplemented method 'call'");
			}
		};

		assertThat(dummy.dimensions()).isEqualTo(3);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/embedding/embedding-model-dimensions.properties", numLinesToSkip = 1, delimiter = '=')
	public void testKnownEmbeddingModelDimensions(String model, String dimension) {
		assertThat(AbstractEmbeddingModel.dimensions(this.embeddingModel, model, "Hello world!"))
			.isEqualTo(Integer.valueOf(dimension));
		verify(this.embeddingModel, never()).embed(any(String.class));
		verify(this.embeddingModel, never()).embed(any(Document.class));
	}

	@Test
	public void testUnknownModelDimension() {
		given(this.embeddingModel.embed(eq("Hello world!"))).willReturn(new float[] { 0.1f, 0.1f, 0.1f });
		assertThat(AbstractEmbeddingModel.dimensions(this.embeddingModel, "unknown_model", "Hello world!"))
			.isEqualTo(3);
	}

}
