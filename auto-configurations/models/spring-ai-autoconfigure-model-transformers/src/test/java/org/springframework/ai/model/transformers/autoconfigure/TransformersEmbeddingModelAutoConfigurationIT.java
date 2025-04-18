package org.springframework.ai.model.transformers.autoconfigure;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class TransformersEmbeddingModelAutoConfigurationIT {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(TransformersEmbeddingModelAutoConfiguration.class));

	@TempDir
	File tempDir;

	@Test
	public void embedding() {
		this.contextRunner.run(context -> {
			var properties = context.getBean(TransformersEmbeddingModelProperties.class);
			assertThat(properties.getCache().isEnabled()).isTrue();
			assertThat(properties.getCache().getDirectory()).isEqualTo(
					new File(System.getProperty("java.io.tmpdir"), "spring-ai-onnx-generative").getAbsolutePath());

			EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
			assertThat(embeddingModel).isInstanceOf(TransformersEmbeddingModel.class);

			List<float[]> embeddings = embeddingModel.embed(List.of("Spring Framework", "Spring AI"));

			assertThat(embeddings.size()).isEqualTo(2);
			assertThat(embeddings.get(0).length).isEqualTo(embeddingModel.dimensions());

		});
	}

	@Test
	public void remoteOnnxModel() {

		this.contextRunner.withPropertyValues(
				"spring.ai.embedding.transformer.cache.directory=" + this.tempDir.getAbsolutePath(),
				"spring.ai.embedding.transformer.onnx.modelUri=https://huggingface.co/intfloat/e5-small-v2/resolve/main/model.onnx",
				"spring.ai.embedding.transformer.tokenizer.uri=https://huggingface.co/intfloat/e5-small-v2/raw/main/tokenizer.json")
			.run(context -> {
				var properties = context.getBean(TransformersEmbeddingModelProperties.class);
				assertThat(properties.getOnnx().getModelUri())
					.isEqualTo("https://huggingface.co/intfloat/e5-small-v2/resolve/main/model.onnx");
				assertThat(properties.getTokenizer().getUri())
					.isEqualTo("https://huggingface.co/intfloat/e5-small-v2/raw/main/tokenizer.json");

				assertThat(properties.getCache().isEnabled()).isTrue();
				assertThat(properties.getCache().getDirectory()).isEqualTo(this.tempDir.getAbsolutePath());
				assertThat(this.tempDir.listFiles()).hasSize(2);

				EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
				assertThat(embeddingModel).isInstanceOf(TransformersEmbeddingModel.class);

				assertThat(embeddingModel.dimensions()).isEqualTo(384);

				List<float[]> embeddings = embeddingModel.embed(List.of("Spring Framework", "Spring AI"));

				assertThat(embeddings.size()).isEqualTo(2);
				assertThat(embeddings.get(0).length).isEqualTo(embeddingModel.dimensions());

			});
	}

	@Test
	void embeddingActivation() {
		this.contextRunner.withPropertyValues("spring.ai.model.embedding=none").run(context -> {
			assertThat(context.getBeansOfType(TransformersEmbeddingModelProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(TransformersEmbeddingModel.class)).isEmpty();
		});

		this.contextRunner.withPropertyValues("spring.ai.model.embedding=transformers").run(context -> {
			assertThat(context.getBeansOfType(TransformersEmbeddingModelProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(TransformersEmbeddingModel.class)).isNotEmpty();
		});

		this.contextRunner.run(context -> {
			assertThat(context.getBeansOfType(TransformersEmbeddingModelProperties.class)).isNotEmpty();
			assertThat(context.getBeansOfType(TransformersEmbeddingModel.class)).isNotEmpty();
		});

	}

}
