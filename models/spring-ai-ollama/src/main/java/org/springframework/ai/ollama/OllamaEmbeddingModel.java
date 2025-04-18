package org.springframework.ai.ollama;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaApi.EmbeddingsResponse;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class OllamaEmbeddingModel extends AbstractEmbeddingModel {

	private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

	private final OllamaApi ollamaApi;

	private final OllamaOptions defaultOptions;

	private final ObservationRegistry observationRegistry;

	private final OllamaModelManager modelManager;

	private EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public OllamaEmbeddingModel(OllamaApi ollamaApi, OllamaOptions defaultOptions,
			ObservationRegistry observationRegistry, ModelManagementOptions modelManagementOptions) {
		Assert.notNull(ollamaApi, "ollamaApi must not be null");
		Assert.notNull(defaultOptions, "options must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		Assert.notNull(modelManagementOptions, "modelManagementOptions must not be null");

		this.ollamaApi = ollamaApi;
		this.defaultOptions = defaultOptions;
		this.observationRegistry = observationRegistry;
		this.modelManager = new OllamaModelManager(ollamaApi, modelManagementOptions);

		initializeModel(defaultOptions.getModel(), modelManagementOptions.pullModelStrategy());
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public float[] embed(Document document) {
		return embed(document.getText());
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {
		Assert.notEmpty(request.getInstructions(), "At least one text is required!");

		EmbeddingRequest embeddingRequest = buildEmbeddingRequest(request);

		OllamaApi.EmbeddingsRequest ollamaEmbeddingRequest = ollamaEmbeddingRequest(embeddingRequest);

		var observationContext = EmbeddingModelObservationContext.builder()
			.embeddingRequest(request)
			.provider(OllamaApi.PROVIDER_NAME)
			.requestOptions(embeddingRequest.getOptions())
			.build();

		return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				EmbeddingsResponse response = this.ollamaApi.embed(ollamaEmbeddingRequest);

				AtomicInteger indexCounter = new AtomicInteger(0);

				List<Embedding> embeddings = response.embeddings()
					.stream()
					.map(e -> new Embedding(e, indexCounter.getAndIncrement()))
					.toList();

				EmbeddingResponseMetadata embeddingResponseMetadata = new EmbeddingResponseMetadata(response.model(),
						getDefaultUsage(response));

				EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, embeddingResponseMetadata);

				observationContext.setResponse(embeddingResponse);

				return embeddingResponse;
			});
	}

	private DefaultUsage getDefaultUsage(OllamaApi.EmbeddingsResponse response) {
		return new DefaultUsage(Optional.ofNullable(response.promptEvalCount()).orElse(0), 0);
	}

	EmbeddingRequest buildEmbeddingRequest(EmbeddingRequest embeddingRequest) {

		OllamaOptions runtimeOptions = null;
		if (embeddingRequest.getOptions() != null) {
			runtimeOptions = ModelOptionsUtils.copyToTarget(embeddingRequest.getOptions(), EmbeddingOptions.class,
					OllamaOptions.class);
		}

		OllamaOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
				OllamaOptions.class);

		if (!StringUtils.hasText(requestOptions.getModel())) {
			throw new IllegalArgumentException("model cannot be null or empty");
		}

		return new EmbeddingRequest(embeddingRequest.getInstructions(), requestOptions);
	}

	OllamaApi.EmbeddingsRequest ollamaEmbeddingRequest(EmbeddingRequest embeddingRequest) {
		OllamaOptions requestOptions = (OllamaOptions) embeddingRequest.getOptions();
		return new OllamaApi.EmbeddingsRequest(requestOptions.getModel(), embeddingRequest.getInstructions(),
				DurationParser.parse(requestOptions.getKeepAlive()),
				OllamaOptions.filterNonSupportedFields(requestOptions.toMap()), requestOptions.getTruncate());
	}

	private void initializeModel(String model, PullModelStrategy pullModelStrategy) {
		if (pullModelStrategy != null && !PullModelStrategy.NEVER.equals(pullModelStrategy)) {
			this.modelManager.pullModel(model, pullModelStrategy);
		}
	}

	public void setObservationConvention(EmbeddingModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention cannot be null");
		this.observationConvention = observationConvention;
	}

	public static class DurationParser {

		private static final Pattern PATTERN = Pattern.compile("(-?\\d+)(ms|s|m|h)");

		public static Duration parse(String input) {

			if (!StringUtils.hasText(input)) {
				return null;
			}

			Matcher matcher = PATTERN.matcher(input);

			if (matcher.matches()) {
				long value = Long.parseLong(matcher.group(1));
				String unit = matcher.group(2);

				return switch (unit) {
					case "ms" -> Duration.ofMillis(value);
					case "s" -> Duration.ofSeconds(value);
					case "m" -> Duration.ofMinutes(value);
					case "h" -> Duration.ofHours(value);
					default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
				};
			}
			else {
				throw new IllegalArgumentException("Invalid duration format: " + input);
			}
		}

	}

	public static final class Builder {

		private OllamaApi ollamaApi;

		private OllamaOptions defaultOptions = OllamaOptions.builder()
			.model(OllamaModel.MXBAI_EMBED_LARGE.id())
			.build();

		private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

		private ModelManagementOptions modelManagementOptions = ModelManagementOptions.defaults();

		private Builder() {
		}

		public Builder ollamaApi(OllamaApi ollamaApi) {
			this.ollamaApi = ollamaApi;
			return this;
		}

		public Builder defaultOptions(OllamaOptions defaultOptions) {
			this.defaultOptions = defaultOptions;
			return this;
		}

		public Builder observationRegistry(ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}

		public Builder modelManagementOptions(ModelManagementOptions modelManagementOptions) {
			this.modelManagementOptions = modelManagementOptions;
			return this;
		}

		public OllamaEmbeddingModel build() {
			return new OllamaEmbeddingModel(this.ollamaApi, this.defaultOptions, this.observationRegistry,
					this.modelManagementOptions);
		}

	}

}
