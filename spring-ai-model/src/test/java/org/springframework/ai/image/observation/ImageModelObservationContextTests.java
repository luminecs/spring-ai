package org.springframework.ai.image.observation;

import org.junit.jupiter.api.Test;

import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageModelObservationContextTests {

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt())
			.provider("superprovider")
			.requestOptions(ImageOptionsBuilder.builder().model("supersun").build())
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenRequestOptionsIsNullThenThrow() {
		assertThatThrownBy(() -> ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt())
			.provider("superprovider")
			.requestOptions(null)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("requestOptions cannot be null");
	}

	private ImagePrompt generateImagePrompt() {
		return new ImagePrompt("here comes the sun");
	}

}
