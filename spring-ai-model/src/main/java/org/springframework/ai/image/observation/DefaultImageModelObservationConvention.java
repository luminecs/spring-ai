package org.springframework.ai.image.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.util.StringUtils;

public class DefaultImageModelObservationConvention implements ImageModelObservationConvention {

	public static final String DEFAULT_NAME = "gen_ai.client.operation";

	private static final KeyValue REQUEST_MODEL_NONE = KeyValue
		.of(ImageModelObservationDocumentation.LowCardinalityKeyNames.REQUEST_MODEL, KeyValue.NONE_VALUE);

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

	@Override
	public String getContextualName(ImageModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getModel())) {
			return "%s %s".formatted(context.getOperationMetadata().operationType(),
					context.getRequest().getOptions().getModel());
		}
		return context.getOperationMetadata().operationType();
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(ImageModelObservationContext context) {
		return KeyValues.of(aiOperationType(context), aiProvider(context), requestModel(context));
	}

	protected KeyValue aiOperationType(ImageModelObservationContext context) {
		return KeyValue.of(ImageModelObservationDocumentation.LowCardinalityKeyNames.AI_OPERATION_TYPE,
				context.getOperationMetadata().operationType());
	}

	protected KeyValue aiProvider(ImageModelObservationContext context) {
		return KeyValue.of(ImageModelObservationDocumentation.LowCardinalityKeyNames.AI_PROVIDER,
				context.getOperationMetadata().provider());
	}

	protected KeyValue requestModel(ImageModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getModel())) {
			return KeyValue.of(ImageModelObservationDocumentation.LowCardinalityKeyNames.REQUEST_MODEL,
					context.getRequest().getOptions().getModel());
		}
		return REQUEST_MODEL_NONE;
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(ImageModelObservationContext context) {
		var keyValues = KeyValues.empty();

		keyValues = requestImageFormat(keyValues, context);
		keyValues = requestImageSize(keyValues, context);
		keyValues = requestImageStyle(keyValues, context);
		return keyValues;
	}

	protected KeyValues requestImageFormat(KeyValues keyValues, ImageModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getResponseFormat())) {
			return keyValues.and(
					ImageModelObservationDocumentation.HighCardinalityKeyNames.REQUEST_IMAGE_RESPONSE_FORMAT.asString(),
					context.getRequest().getOptions().getResponseFormat());
		}
		return keyValues;
	}

	protected KeyValues requestImageSize(KeyValues keyValues, ImageModelObservationContext context) {
		if (context.getRequest().getOptions().getWidth() != null
				&& context.getRequest().getOptions().getHeight() != null) {
			return keyValues.and(
					ImageModelObservationDocumentation.HighCardinalityKeyNames.REQUEST_IMAGE_SIZE.asString(),
					"%sx%s".formatted(context.getRequest().getOptions().getWidth(),
							context.getRequest().getOptions().getHeight()));
		}
		return keyValues;
	}

	protected KeyValues requestImageStyle(KeyValues keyValues, ImageModelObservationContext context) {
		if (StringUtils.hasText(context.getRequest().getOptions().getStyle())) {
			return keyValues.and(
					ImageModelObservationDocumentation.HighCardinalityKeyNames.REQUEST_IMAGE_STYLE.asString(),
					context.getRequest().getOptions().getStyle());
		}
		return keyValues;
	}

}
