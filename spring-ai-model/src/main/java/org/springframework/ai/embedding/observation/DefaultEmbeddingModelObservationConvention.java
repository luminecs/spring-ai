package org.springframework.ai.embedding.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.util.StringUtils;

public class DefaultEmbeddingModelObservationConvention implements EmbeddingModelObservationConvention {

	public static final String DEFAULT_NAME = "gen_ai.client.operation";

	private static final KeyValue REQUEST_MODEL_NONE = KeyValue
		.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.REQUEST_MODEL, KeyValue.NONE_VALUE);

	private static final KeyValue RESPONSE_MODEL_NONE = KeyValue
		.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.RESPONSE_MODEL, KeyValue.NONE_VALUE);

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

	@Override
	public String getContextualName(EmbeddingModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getModel())) {
			return "%s %s".formatted(context.getOperationMetadata().operationType(),
					context.getRequest().getOptions().getModel());
		}
		return context.getOperationMetadata().operationType();
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(EmbeddingModelObservationContext context) {
		return KeyValues.of(aiOperationType(context), aiProvider(context), requestModel(context),
				responseModel(context));
	}

	protected KeyValue aiOperationType(EmbeddingModelObservationContext context) {
		return KeyValue.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.AI_OPERATION_TYPE,
				context.getOperationMetadata().operationType());
	}

	protected KeyValue aiProvider(EmbeddingModelObservationContext context) {
		return KeyValue.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.AI_PROVIDER,
				context.getOperationMetadata().provider());
	}

	protected KeyValue requestModel(EmbeddingModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getModel())) {
			return KeyValue.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.REQUEST_MODEL,
					context.getRequest().getOptions().getModel());
		}
		return REQUEST_MODEL_NONE;
	}

	protected KeyValue responseModel(EmbeddingModelObservationContext context) {
		if (context.getResponse() != null && context.getResponse().getMetadata() != null
				&& StringUtils.hasText(context.getResponse().getMetadata().getModel())) {
			return KeyValue.of(EmbeddingModelObservationDocumentation.LowCardinalityKeyNames.RESPONSE_MODEL,
					context.getResponse().getMetadata().getModel());
		}
		return RESPONSE_MODEL_NONE;
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(EmbeddingModelObservationContext context) {
		var keyValues = KeyValues.empty();

		keyValues = requestEmbeddingDimension(keyValues, context);

		keyValues = usageInputTokens(keyValues, context);
		keyValues = usageTotalTokens(keyValues, context);
		return keyValues;
	}

	protected KeyValues requestEmbeddingDimension(KeyValues keyValues, EmbeddingModelObservationContext context) {
		if (context.getRequest().getOptions().getDimensions() != null) {
			return keyValues
				.and(EmbeddingModelObservationDocumentation.HighCardinalityKeyNames.REQUEST_EMBEDDING_DIMENSIONS
					.asString(), String.valueOf(context.getRequest().getOptions().getDimensions()));
		}
		return keyValues;
	}

	protected KeyValues usageInputTokens(KeyValues keyValues, EmbeddingModelObservationContext context) {
		if (context.getResponse() != null && context.getResponse().getMetadata() != null
				&& context.getResponse().getMetadata().getUsage() != null
				&& context.getResponse().getMetadata().getUsage().getPromptTokens() != null) {
			return keyValues.and(
					EmbeddingModelObservationDocumentation.HighCardinalityKeyNames.USAGE_INPUT_TOKENS.asString(),
					String.valueOf(context.getResponse().getMetadata().getUsage().getPromptTokens()));
		}
		return keyValues;
	}

	protected KeyValues usageTotalTokens(KeyValues keyValues, EmbeddingModelObservationContext context) {
		if (context.getResponse() != null && context.getResponse().getMetadata() != null
				&& context.getResponse().getMetadata().getUsage() != null
				&& context.getResponse().getMetadata().getUsage().getTotalTokens() != null) {
			return keyValues.and(
					EmbeddingModelObservationDocumentation.HighCardinalityKeyNames.USAGE_TOTAL_TOKENS.asString(),
					String.valueOf(context.getResponse().getMetadata().getUsage().getTotalTokens()));
		}
		return keyValues;
	}

}
