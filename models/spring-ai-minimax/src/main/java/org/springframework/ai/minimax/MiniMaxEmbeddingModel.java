package org.springframework.ai.minimax;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.metadata.DefaultUsage;
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
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.minimax.api.MiniMaxApiConstants;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class MiniMaxEmbeddingModel extends AbstractEmbeddingModel {

	private static final Logger logger = LoggerFactory.getLogger(MiniMaxEmbeddingModel.class);

	private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

	private final MiniMaxEmbeddingOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final MiniMaxApi miniMaxApi;

	private final MetadataMode metadataMode;

	private final ObservationRegistry observationRegistry;

	private EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public MiniMaxEmbeddingModel(MiniMaxApi miniMaxApi) {
		this(miniMaxApi, MetadataMode.EMBED);
	}

	public MiniMaxEmbeddingModel(MiniMaxApi miniMaxApi, MetadataMode metadataMode) {
		this(miniMaxApi, metadataMode,
				MiniMaxEmbeddingOptions.builder().model(MiniMaxApi.DEFAULT_EMBEDDING_MODEL).build(),
				RetryUtils.DEFAULT_RETRY_TEMPLATE, ObservationRegistry.NOOP);
	}

	public MiniMaxEmbeddingModel(MiniMaxApi miniMaxApi, MetadataMode metadataMode,
			MiniMaxEmbeddingOptions miniMaxEmbeddingOptions) {
		this(miniMaxApi, metadataMode, miniMaxEmbeddingOptions, RetryUtils.DEFAULT_RETRY_TEMPLATE,
				ObservationRegistry.NOOP);
	}

	public MiniMaxEmbeddingModel(MiniMaxApi miniMaxApi, MetadataMode metadataMode,
			MiniMaxEmbeddingOptions miniMaxEmbeddingOptions, RetryTemplate retryTemplate) {
		this(miniMaxApi, metadataMode, miniMaxEmbeddingOptions, retryTemplate, ObservationRegistry.NOOP);
	}

	public MiniMaxEmbeddingModel(MiniMaxApi miniMaxApi, MetadataMode metadataMode, MiniMaxEmbeddingOptions options,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
		Assert.notNull(miniMaxApi, "MiniMaxApi must not be null");
		Assert.notNull(metadataMode, "metadataMode must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");

		this.miniMaxApi = miniMaxApi;
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

		MiniMaxApi.EmbeddingRequest apiRequest = new MiniMaxApi.EmbeddingRequest(request.getInstructions(),
				embeddingRequest.getOptions().getModel());

		var observationContext = EmbeddingModelObservationContext.builder()
			.embeddingRequest(request)
			.provider(MiniMaxApiConstants.PROVIDER_NAME)
			.build();

		return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				MiniMaxApi.EmbeddingList apiEmbeddingResponse = this.retryTemplate
					.execute(ctx -> this.miniMaxApi.embeddings(apiRequest).getBody());

				if (apiEmbeddingResponse == null) {
					logger.warn("No embeddings returned for request: {}", request);
					return new EmbeddingResponse(List.of());
				}

				var metadata = new EmbeddingResponseMetadata(apiRequest.model(), getDefaultUsage(apiEmbeddingResponse));

				List<Embedding> embeddings = new ArrayList<>();
				for (int i = 0; i < apiEmbeddingResponse.vectors().size(); i++) {
					float[] vector = apiEmbeddingResponse.vectors().get(i);
					embeddings.add(new Embedding(vector, i));
				}
				EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, metadata);
				observationContext.setResponse(embeddingResponse);
				return embeddingResponse;
			});
	}

	private DefaultUsage getDefaultUsage(MiniMaxApi.EmbeddingList apiEmbeddingList) {
		return new DefaultUsage(0, 0, apiEmbeddingList.totalTokens());
	}

	EmbeddingRequest buildEmbeddingRequest(EmbeddingRequest embeddingRequest) {

		MiniMaxEmbeddingOptions runtimeOptions = null;
		if (embeddingRequest.getOptions() != null) {
			runtimeOptions = ModelOptionsUtils.copyToTarget(embeddingRequest.getOptions(), EmbeddingOptions.class,
					MiniMaxEmbeddingOptions.class);
		}

		MiniMaxEmbeddingOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
				MiniMaxEmbeddingOptions.class);

		if (!StringUtils.hasText(requestOptions.getModel())) {
			throw new IllegalArgumentException("model cannot be null or empty");
		}

		return new EmbeddingRequest(embeddingRequest.getInstructions(), requestOptions);
	}

	public void setObservationConvention(EmbeddingModelObservationConvention observationConvention) {
		this.observationConvention = observationConvention;
	}

}
