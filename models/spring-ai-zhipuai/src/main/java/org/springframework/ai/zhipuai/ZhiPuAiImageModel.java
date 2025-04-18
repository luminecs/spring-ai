package org.springframework.ai.zhipuai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class ZhiPuAiImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(ZhiPuAiImageModel.class);

	public final RetryTemplate retryTemplate;

	private final ZhiPuAiImageOptions defaultOptions;

	private final ZhiPuAiImageApi zhiPuAiImageApi;

	public ZhiPuAiImageModel(ZhiPuAiImageApi zhiPuAiImageApi) {
		this(zhiPuAiImageApi, ZhiPuAiImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public ZhiPuAiImageModel(ZhiPuAiImageApi zhiPuAiImageApi, ZhiPuAiImageOptions defaultOptions,
			RetryTemplate retryTemplate) {
		Assert.notNull(zhiPuAiImageApi, "ZhiPuAiImageApi must not be null");
		Assert.notNull(defaultOptions, "defaultOptions must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		this.zhiPuAiImageApi = zhiPuAiImageApi;
		this.defaultOptions = defaultOptions;
		this.retryTemplate = retryTemplate;
	}

	public ZhiPuAiImageOptions getDefaultOptions() {
		return this.defaultOptions;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		return this.retryTemplate.execute(ctx -> {

			String instructions = imagePrompt.getInstructions().get(0).getText();

			ZhiPuAiImageApi.ZhiPuAiImageRequest imageRequest = new ZhiPuAiImageApi.ZhiPuAiImageRequest(instructions,
					ZhiPuAiImageApi.DEFAULT_IMAGE_MODEL);

			if (this.defaultOptions != null) {
				imageRequest = ModelOptionsUtils.merge(this.defaultOptions, imageRequest,
						ZhiPuAiImageApi.ZhiPuAiImageRequest.class);
			}

			if (imagePrompt.getOptions() != null) {
				imageRequest = ModelOptionsUtils.merge(toZhiPuAiImageOptions(imagePrompt.getOptions()), imageRequest,
						ZhiPuAiImageApi.ZhiPuAiImageRequest.class);
			}

			ResponseEntity<ZhiPuAiImageApi.ZhiPuAiImageResponse> imageResponseEntity = this.zhiPuAiImageApi
				.createImage(imageRequest);

			return convertResponse(imageResponseEntity, imageRequest);
		});
	}

	private ImageResponse convertResponse(ResponseEntity<ZhiPuAiImageApi.ZhiPuAiImageResponse> imageResponseEntity,
			ZhiPuAiImageApi.ZhiPuAiImageRequest zhiPuAiImageRequest) {
		ZhiPuAiImageApi.ZhiPuAiImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("No image response returned for request: {}", zhiPuAiImageRequest);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data()
			.stream()
			.map(entry -> new ImageGeneration(new Image(entry.url(), null)))
			.toList();

		return new ImageResponse(imageGenerationList);
	}

	private ZhiPuAiImageOptions toZhiPuAiImageOptions(ImageOptions runtimeImageOptions) {
		ZhiPuAiImageOptions.Builder zhiPuAiImageOptionsBuilder = ZhiPuAiImageOptions.builder();
		if (runtimeImageOptions != null) {
			if (runtimeImageOptions.getModel() != null) {
				zhiPuAiImageOptionsBuilder.model(runtimeImageOptions.getModel());
			}
			if (runtimeImageOptions instanceof ZhiPuAiImageOptions runtimeZhiPuAiImageOptions) {
				if (runtimeZhiPuAiImageOptions.getUser() != null) {
					zhiPuAiImageOptionsBuilder.user(runtimeZhiPuAiImageOptions.getUser());
				}
			}
		}
		return zhiPuAiImageOptionsBuilder.build();
	}

}
