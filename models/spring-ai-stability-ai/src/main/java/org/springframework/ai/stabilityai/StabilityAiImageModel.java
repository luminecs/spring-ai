package org.springframework.ai.stabilityai;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.util.Assert;

public class StabilityAiImageModel implements ImageModel {

	private final StabilityAiImageOptions defaultOptions;

	private final StabilityAiApi stabilityAiApi;

	public StabilityAiImageModel(StabilityAiApi stabilityAiApi) {
		this(stabilityAiApi, StabilityAiImageOptions.builder().build());
	}

	public StabilityAiImageModel(StabilityAiApi stabilityAiApi, StabilityAiImageOptions defaultOptions) {
		Assert.notNull(stabilityAiApi, "StabilityAiApi must not be null");
		Assert.notNull(defaultOptions, "StabilityAiImageOptions must not be null");
		this.stabilityAiApi = stabilityAiApi;
		this.defaultOptions = defaultOptions;
	}

	private static StabilityAiApi.GenerateImageRequest getGenerateImageRequest(ImagePrompt stabilityAiImagePrompt,
			StabilityAiImageOptions optionsToUse) {
		return new StabilityAiApi.GenerateImageRequest.Builder()
			.textPrompts(stabilityAiImagePrompt.getInstructions()
				.stream()
				.map(message -> new StabilityAiApi.GenerateImageRequest.TextPrompts(message.getText(),
						message.getWeight()))
				.collect(Collectors.toList()))
			.height(optionsToUse.getHeight())
			.width(optionsToUse.getWidth())
			.cfgScale(optionsToUse.getCfgScale())
			.clipGuidancePreset(optionsToUse.getClipGuidancePreset())
			.sampler(optionsToUse.getSampler())
			.samples(optionsToUse.getN())
			.seed(optionsToUse.getSeed())
			.steps(optionsToUse.getSteps())
			.stylePreset(optionsToUse.getStylePreset())
			.build();
	}

	public StabilityAiImageOptions getOptions() {
		return this.defaultOptions;
	}

	public ImageResponse call(ImagePrompt imagePrompt) {

		StabilityAiImageOptions requestImageOptions = mergeOptions(imagePrompt.getOptions(), this.defaultOptions);

		StabilityAiApi.GenerateImageRequest generateImageRequest = getGenerateImageRequest(imagePrompt,
				requestImageOptions);

		StabilityAiApi.GenerateImageResponse generateImageResponse = this.stabilityAiApi
			.generateImage(generateImageRequest);

		return convertResponse(generateImageResponse);
	}

	private ImageResponse convertResponse(StabilityAiApi.GenerateImageResponse generateImageResponse) {
		List<ImageGeneration> imageGenerationList = generateImageResponse.artifacts()
			.stream()
			.map(entry -> new ImageGeneration(new Image(null, entry.base64()),
					new StabilityAiImageGenerationMetadata(entry.finishReason(), entry.seed())))
			.toList();

		return new ImageResponse(imageGenerationList, new ImageResponseMetadata());
	}

	StabilityAiImageOptions mergeOptions(ImageOptions runtimeOptions, StabilityAiImageOptions defaultOptions) {
		if (runtimeOptions == null) {
			return defaultOptions;
		}
		StabilityAiImageOptions.Builder builder = StabilityAiImageOptions.builder()

			.model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), defaultOptions.getModel()))
			.N(ModelOptionsUtils.mergeOption(runtimeOptions.getN(), defaultOptions.getN()))
			.responseFormat(ModelOptionsUtils.mergeOption(runtimeOptions.getResponseFormat(),
					defaultOptions.getResponseFormat()))
			.width(ModelOptionsUtils.mergeOption(runtimeOptions.getWidth(), defaultOptions.getWidth()))
			.height(ModelOptionsUtils.mergeOption(runtimeOptions.getHeight(), defaultOptions.getHeight()))
			.stylePreset(ModelOptionsUtils.mergeOption(runtimeOptions.getStyle(), defaultOptions.getStyle()))

			.cfgScale(defaultOptions.getCfgScale())
			.clipGuidancePreset(defaultOptions.getClipGuidancePreset())
			.sampler(defaultOptions.getSampler())
			.seed(defaultOptions.getSeed())
			.steps(defaultOptions.getSteps())
			.stylePreset(defaultOptions.getStylePreset());
		if (runtimeOptions instanceof StabilityAiImageOptions) {
			StabilityAiImageOptions stabilityOptions = (StabilityAiImageOptions) runtimeOptions;

			builder
				.cfgScale(ModelOptionsUtils.mergeOption(stabilityOptions.getCfgScale(), defaultOptions.getCfgScale()))
				.clipGuidancePreset(ModelOptionsUtils.mergeOption(stabilityOptions.getClipGuidancePreset(),
						defaultOptions.getClipGuidancePreset()))
				.sampler(ModelOptionsUtils.mergeOption(stabilityOptions.getSampler(), defaultOptions.getSampler()))
				.seed(ModelOptionsUtils.mergeOption(stabilityOptions.getSeed(), defaultOptions.getSeed()))
				.steps(ModelOptionsUtils.mergeOption(stabilityOptions.getSteps(), defaultOptions.getSteps()))
				.stylePreset(ModelOptionsUtils.mergeOption(stabilityOptions.getStylePreset(),
						defaultOptions.getStylePreset()));
		}

		return builder.build();
	}

}
