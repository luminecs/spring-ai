package org.springframework.ai.image.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.observation.conventions.AiObservationAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.image.observation.ImageModelObservationDocumentation.HighCardinalityKeyNames;

class DefaultImageModelObservationConventionTests {

	private final DefaultImageModelObservationConvention observationConvention = new DefaultImageModelObservationConvention();

	@Test
	void shouldHaveName() {
		assertThat(this.observationConvention.getName()).isEqualTo(DefaultImageModelObservationConvention.DEFAULT_NAME);
	}

	@Test
	void contextualNameWhenModelIsDefined() {
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		assertThat(this.observationConvention.getContextualName(observationContext)).isEqualTo("image mistral");
	}

	@Test
	void contextualNameWhenModelIsNotDefined() {
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().build()))
			.provider("superprovider")
			.build();
		assertThat(this.observationConvention.getContextualName(observationContext)).isEqualTo("image");
	}

	@Test
	void supportsOnlyImageModelObservationContext() {
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		assertThat(this.observationConvention.supportsContext(observationContext)).isTrue();
		assertThat(this.observationConvention.supportsContext(new Observation.Context())).isFalse();
	}

	@Test
	void shouldHaveLowCardinalityKeyValuesWhenDefined() {
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().model("mistral").build()))
			.provider("superprovider")
			.build();
		assertThat(this.observationConvention.getLowCardinalityKeyValues(observationContext)).contains(
				KeyValue.of(AiObservationAttributes.AI_OPERATION_TYPE.value(), "image"),
				KeyValue.of(AiObservationAttributes.AI_PROVIDER.value(), "superprovider"),
				KeyValue.of(AiObservationAttributes.REQUEST_MODEL.value(), "mistral"));
	}

	@Test
	void shouldHaveHighCardinalityKeyValuesWhenDefined() {
		var imageOptions = ImageOptionsBuilder.builder()
			.model("mistral")
			.N(1)
			.height(1080)
			.width(1920)
			.style("sketch")
			.responseFormat("base64")
			.build();
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(imageOptions))
			.provider("superprovider")
			.build();

		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext)).contains(
				KeyValue.of(AiObservationAttributes.REQUEST_IMAGE_RESPONSE_FORMAT.value(), "base64"),
				KeyValue.of(AiObservationAttributes.REQUEST_IMAGE_SIZE.value(), "1920x1080"),
				KeyValue.of(AiObservationAttributes.REQUEST_IMAGE_STYLE.value(), "sketch"));
	}

	@Test
	void shouldNotHaveKeyValuesWhenEmptyValues() {
		ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().build()))
			.provider("superprovider")
			.build();

		assertThat(this.observationConvention.getLowCardinalityKeyValues(observationContext))
			.contains(KeyValue.of(AiObservationAttributes.REQUEST_MODEL.value(), KeyValue.NONE_VALUE));
		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext)
			.stream()
			.map(KeyValue::getKey)
			.toList()).doesNotContain(HighCardinalityKeyNames.REQUEST_IMAGE_RESPONSE_FORMAT.asString(),
					HighCardinalityKeyNames.REQUEST_IMAGE_SIZE.asString(),
					HighCardinalityKeyNames.REQUEST_IMAGE_STYLE.asString());
	}

	private ImagePrompt generateImagePrompt(ImageOptions imageOptions) {
		return new ImagePrompt("here comes the sun", imageOptions);
	}

}
