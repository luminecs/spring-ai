package org.springframework.ai.embedding;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

@ImportRuntimeHints(AbstractEmbeddingModel.Hints.class)
public abstract class AbstractEmbeddingModel implements EmbeddingModel {

	private static final Resource EMBEDDING_MODEL_DIMENSIONS_PROPERTIES = new ClassPathResource(
			"/embedding/embedding-model-dimensions.properties");

	private static final Map<String, Integer> KNOWN_EMBEDDING_DIMENSIONS = loadKnownModelDimensions();

	protected final AtomicInteger embeddingDimensions = new AtomicInteger(-1);

	public static int dimensions(EmbeddingModel embeddingModel, String modelName, String dummyContent) {

		if (KNOWN_EMBEDDING_DIMENSIONS.containsKey(modelName)) {

			return KNOWN_EMBEDDING_DIMENSIONS.get(modelName);
		}
		else {

			return embeddingModel.embed(dummyContent).length;
		}
	}

	private static Map<String, Integer> loadKnownModelDimensions() {
		try {
			var resource = EMBEDDING_MODEL_DIMENSIONS_PROPERTIES;
			Assert.notNull(resource, "the embedding dimensions must be non-null");
			Assert.state(resource.exists(), "the embedding dimensions properties file must exist");
			var properties = new Properties();
			try (var in = resource.getInputStream()) {
				properties.load(in);
			}
			return properties.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> Integer.parseInt(e.getValue().toString())));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int dimensions() {
		if (this.embeddingDimensions.get() < 0) {
			this.embeddingDimensions.set(dimensions(this, "Test", "Hello World"));
		}
		return this.embeddingDimensions.get();
	}

	static class Hints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.resources().registerResource(EMBEDDING_MODEL_DIMENSIONS_PROPERTIES);
		}

	}

}
