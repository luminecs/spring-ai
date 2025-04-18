package org.springframework.ai.stabilityai;

import org.junit.jupiter.api.Test;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StabilityAiImageOptionsTests {

	@Test
	void shouldPreferRuntimeOptionsOverDefaultOptions() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);

		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.N(1)
			.model("default-model")
			.width(512)
			.height(512)
			.responseFormat("image/png")
			.cfgScale(7.0f)
			.clipGuidancePreset("FAST_BLUE")
			.sampler("DDIM")
			.seed(1234L)
			.steps(30)
			.stylePreset("3d-model")
			.build();

		StabilityAiImageOptions runtimeOptions = StabilityAiImageOptions.builder()
			.N(2)
			.model("runtime-model")
			.width(1024)
			.height(768)
			.responseFormat("application/json")
			.cfgScale(14.0f)
			.clipGuidancePreset("FAST_GREEN")
			.sampler("DDPM")
			.seed(5678L)
			.steps(50)
			.stylePreset("anime")
			.build();

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(runtimeOptions, defaultOptions);

		assertThat(mergedOptions).satisfies(options -> {

			assertThat(options.getN()).isEqualTo(2);
			assertThat(options.getModel()).isEqualTo("runtime-model");
			assertThat(options.getWidth()).isEqualTo(1024);
			assertThat(options.getHeight()).isEqualTo(768);
			assertThat(options.getResponseFormat()).isEqualTo("application/json");
			assertThat(options.getCfgScale()).isEqualTo(14.0f);
			assertThat(options.getClipGuidancePreset()).isEqualTo("FAST_GREEN");
			assertThat(options.getSampler()).isEqualTo("DDPM");
			assertThat(options.getSeed()).isEqualTo(5678L);
			assertThat(options.getSteps()).isEqualTo(50);
			assertThat(options.getStylePreset()).isEqualTo("anime");
		});
	}

	@Test
	void shouldUseDefaultOptionsWhenRuntimeOptionsAreNull() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);
		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.N(1)
			.model("default-model")
			.cfgScale(7.0f)
			.build();

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(null, defaultOptions);

		assertThat(mergedOptions).satisfies(options -> {
			assertThat(options.getN()).isEqualTo(1);
			assertThat(options.getModel()).isEqualTo("default-model");
			assertThat(options.getCfgScale()).isEqualTo(7.0f);
		});
	}

	@Test
	void shouldHandleGenericImageOptionsCorrectly() {

		StabilityAiApi stabilityAiApi = mock(StabilityAiApi.class);
		StabilityAiImageOptions defaultOptions = StabilityAiImageOptions.builder()
			.N(1)
			.model("default-model")
			.width(512)
			.cfgScale(7.0f)
			.build();

		ImageOptions genericOptions = new ImageOptions() {
			@Override
			public Integer getN() {
				return 2;
			}

			@Override
			public String getModel() {
				return "generic-model";
			}

			@Override
			public Integer getWidth() {
				return 1024;
			}

			@Override
			public Integer getHeight() {
				return null;
			}

			@Override
			public String getResponseFormat() {
				return null;
			}

			@Override
			public String getStyle() {
				return null;
			}
		};

		StabilityAiImageModel imageModel = new StabilityAiImageModel(stabilityAiApi, defaultOptions);

		StabilityAiImageOptions mergedOptions = imageModel.mergeOptions(genericOptions, defaultOptions);

		assertThat(mergedOptions.getN()).isEqualTo(2);
		assertThat(mergedOptions.getModel()).isEqualTo("generic-model");
		assertThat(mergedOptions.getWidth()).isEqualTo(1024);

		assertThat(mergedOptions.getCfgScale()).isEqualTo(7.0f);
	}

}
