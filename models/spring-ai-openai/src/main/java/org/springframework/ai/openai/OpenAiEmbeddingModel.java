package org.springframework.ai.openai;

import java.util.List;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.EmbeddingList;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class OpenAiEmbeddingModel extends AbstractEmbeddingModel {

	private static final Logger logger = LoggerFactory.getLogger(OpenAiEmbeddingModel.class);

	private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

	private final OpenAiEmbeddingOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final OpenAiApi openAiApi;

	private final MetadataMode metadataMode;

	private final ObservationRegistry observationRegistry;

	private EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public OpenAiEmbeddingModel(OpenAiApi openAiApi) {
		this(openAiApi, MetadataMode.EMBED);
	}

	public OpenAiEmbeddingModel(OpenAiApi openAiApi, MetadataMode metadataMode) {
		this(openAiApi, metadataMode,
				OpenAiEmbeddingOptions.builder().model(OpenAiApi.DEFAULT_EMBEDDING_MODEL).build());
	}

	public OpenAiEmbeddingModel(OpenAiApi openAiApi, MetadataMode metadataMode,
			OpenAiEmbeddingOptions openAiEmbeddingOptions) {
		this(openAiApi, metadataMode, openAiEmbeddingOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public OpenAiEmbeddingModel(OpenAiApi openAiApi, MetadataMode metadataMode, OpenAiEmbeddingOptions options,
			RetryTemplate retryTemplate) {
		this(openAiApi, metadataMode, options, retryTemplate, ObservationRegistry.NOOP);
	}

	public OpenAiEmbeddingModel(OpenAiApi openAiApi, MetadataMode metadataMode, OpenAiEmbeddingOptions options,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
		Assert.notNull(openAiApi, "openAiApi must not be null");
		Assert.notNull(metadataMode, "metadataMode must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");

		this.openAiApi = openAiApi;
		this.metadataMode = metadataMode;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}

	@Override
	public float[] embed(Document document) {
		Assert.notNull(document, "Document must not be null");
		return this.embed(document.getFormattedContent(this.metadataMode));
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		EmbeddingRequest embeddingRequest = buildEmbeddingRequest(request);

		OpenAiApi.EmbeddingRequest<List<String>> apiRequest = createRequest(embeddingRequest);

		var observationContext = EmbeddingModelObservationContext.builder()
			.embeddingRequest(embeddingRequest)
			.provider(OpenAiApiConstants.PROVIDER_NAME)
			.build();

		return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				EmbeddingList<OpenAiApi.Embedding> apiEmbeddingResponse = this.retryTemplate
					.execute(ctx -> this.openAiApi.embeddings(apiRequest).getBody());

				if (apiEmbeddingResponse == null) {
					logger.warn("No embeddings returned for request: {}", request);
					return new EmbeddingResponse(List.of());
				}

				OpenAiApi.Usage usage = apiEmbeddingResponse.usage();
				Usage embeddingResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
				var metadata = new EmbeddingResponseMetadata(apiEmbeddingResponse.model(), embeddingResponseUsage);

				List<Embedding> embeddings = apiEmbeddingResponse.data()
					.stream()
					.map(e -> new Embedding(e.embedding(), e.index()))
					.toList();

				EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, metadata);

				observationContext.setResponse(embeddingResponse);

				return embeddingResponse;
			});
	}

	private DefaultUsage getDefaultUsage(OpenAiApi.Usage usage) {
		return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
	}

	private OpenAiApi.EmbeddingRequest<List<String>> createRequest(EmbeddingRequest request) {
		OpenAiEmbeddingOptions requestOptions = (OpenAiEmbeddingOptions) request.getOptions();
		return new OpenAiApi.EmbeddingRequest<>(request.getInstructions(), requestOptions.getModel(),
				requestOptions.getEncodingFormat(), requestOptions.getDimensions(), requestOptions.getUser());
	}

	private EmbeddingRequest buildEmbeddingRequest(EmbeddingRequest embeddingRequest) {

		OpenAiEmbeddingOptions runtimeOptions = null;
		if (embeddingRequest.getOptions() != null) {
			runtimeOptions = ModelOptionsUtils.copyToTarget(embeddingRequest.getOptions(), EmbeddingOptions.class,
					OpenAiEmbeddingOptions.class);
		}

		OpenAiEmbeddingOptions requestOptions = runtimeOptions == null ? this.defaultOptions : OpenAiEmbeddingOptions
			.builder()

			.model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), this.defaultOptions.getModel()))
			.dimensions(
					ModelOptionsUtils.mergeOption(runtimeOptions.getDimensions(), this.defaultOptions.getDimensions()))

			.encodingFormat(ModelOptionsUtils.mergeOption(runtimeOptions.getEncodingFormat(),
					this.defaultOptions.getEncodingFormat()))
			.user(ModelOptionsUtils.mergeOption(runtimeOptions.getUser(), this.defaultOptions.getUser()))
			.build();

		return new EmbeddingRequest(embeddingRequest.getInstructions(), requestOptions);
	}

	public void setObservationConvention(EmbeddingModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention cannot be null");
		this.observationConvention = observationConvention;
	}

}
