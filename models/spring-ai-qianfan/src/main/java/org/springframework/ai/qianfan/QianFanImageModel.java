package org.springframework.ai.qianfan;

import java.util.List;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.qianfan.api.QianFanConstants;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class QianFanImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(QianFanImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	private final QianFanImageOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final QianFanImageApi qianFanImageApi;

	private final ObservationRegistry observationRegistry;

	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public QianFanImageModel(QianFanImageApi qianFanImageApi) {
		this(qianFanImageApi, QianFanImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public QianFanImageModel(QianFanImageApi qianFanImageApi, QianFanImageOptions options) {
		this(qianFanImageApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public QianFanImageModel(QianFanImageApi qianFanImageApi, QianFanImageOptions options,
			RetryTemplate retryTemplate) {
		this(qianFanImageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	public QianFanImageModel(QianFanImageApi qianFanImageApi, QianFanImageOptions options, RetryTemplate retryTemplate,
			ObservationRegistry observationRegistry) {
		Assert.notNull(qianFanImageApi, "QianFanImageApi must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		this.qianFanImageApi = qianFanImageApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		QianFanImageOptions requestImageOptions = mergeOptions(imagePrompt.getOptions(), this.defaultOptions);

		QianFanImageApi.QianFanImageRequest imageRequest = createRequest(imagePrompt, requestImageOptions);

		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(imagePrompt)
			.provider(QianFanConstants.PROVIDER_NAME)
			.requestOptions(requestImageOptions)
			.build();

		return ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {

				ResponseEntity<QianFanImageApi.QianFanImageResponse> imageResponseEntity = this.retryTemplate
					.execute(ctx -> this.qianFanImageApi.createImage(imageRequest));

				ImageResponse imageResponse = convertResponse(imageResponseEntity, imageRequest);

				observationContext.setResponse(imageResponse);

				return imageResponse;
			});
	}

	private QianFanImageApi.QianFanImageRequest createRequest(ImagePrompt imagePrompt,
			QianFanImageOptions requestImageOptions) {
		String instructions = imagePrompt.getInstructions().get(0).getText();

		QianFanImageApi.QianFanImageRequest imageRequest = new QianFanImageApi.QianFanImageRequest(instructions,
				QianFanImageApi.DEFAULT_IMAGE_MODEL);

		return ModelOptionsUtils.merge(requestImageOptions, imageRequest, QianFanImageApi.QianFanImageRequest.class);
	}

	private ImageResponse convertResponse(ResponseEntity<QianFanImageApi.QianFanImageResponse> imageResponseEntity,
			QianFanImageApi.QianFanImageRequest qianFanImageRequest) {
		QianFanImageApi.QianFanImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("No image response returned for request: {}", qianFanImageRequest);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data()
			.stream()
			.map(entry -> new ImageGeneration(new Image(null, entry.b64Image())))
			.toList();

		return new ImageResponse(imageGenerationList);
	}

	private QianFanImageOptions mergeOptions(@Nullable ImageOptions runtimeImageOptions,
			QianFanImageOptions defaultOptions) {
		var runtimeOptionsForProvider = ModelOptionsUtils.copyToTarget(runtimeImageOptions, ImageOptions.class,
				QianFanImageOptions.class);

		if (runtimeOptionsForProvider == null) {
			return defaultOptions;
		}

		return QianFanImageOptions.builder()
			.model(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getModel(), defaultOptions.getModel()))
			.N(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getN(), defaultOptions.getN()))
			.model(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getModel(), defaultOptions.getModel()))
			.width(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getWidth(), defaultOptions.getWidth()))
			.height(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getHeight(), defaultOptions.getHeight()))
			.style(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getStyle(), defaultOptions.getStyle()))
			.user(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getUser(), defaultOptions.getUser()))
			.build();
	}

	public void setObservationConvention(ImageModelObservationConvention observationConvention) {
		this.observationConvention = observationConvention;
	}

}
