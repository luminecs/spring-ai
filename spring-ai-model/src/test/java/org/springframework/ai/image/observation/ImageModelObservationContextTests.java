package org.springframework.ai.image.observation;

import org.junit.jupiter.api.Test;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageModelObservationContextTests {

	@Test
	void whenMandatoryRequestOptionsThenReturn() {
		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(generateImagePrompt(ImageOptionsBuilder.builder().model("supersun").build()))
			.provider("superprovider")
			.build();

		assertThat(observationContext).isNotNull();
	}

	private ImagePrompt generateImagePrompt(ImageOptions imageOptions) {
		return new ImagePrompt("here comes the sun");
	}

}
