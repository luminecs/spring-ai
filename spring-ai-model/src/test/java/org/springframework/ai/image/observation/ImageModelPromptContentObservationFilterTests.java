package org.springframework.ai.image.observation;

import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.observation.conventions.AiObservationAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class ImageModelPromptContentObservationFilterTests {

	private final ImageModelPromptContentObservationFilter observationFilter = new ImageModelPromptContentObservationFilter();

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyPromptThenReturnOriginalContext() {
		var expectedContext = ImageModelObservationContext.builder()
			.imagePrompt(new ImagePrompt(""))
			.provider("superprovider")
			.requestOptions(ImageOptionsBuilder.builder().model("mistral").build())
			.build();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenPromptWithTextThenAugmentContext() {
		var originalContext = ImageModelObservationContext.builder()
			.imagePrompt(new ImagePrompt("supercalifragilisticexpialidocious"))
			.provider("superprovider")
			.requestOptions(ImageOptionsBuilder.builder().model("mistral").build())
			.build();
		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(AiObservationAttributes.PROMPT.value(), "[\"supercalifragilisticexpialidocious\"]"));
	}

	@Test
	void whenPromptWithMessagesThenAugmentContext() {
		var originalContext = ImageModelObservationContext.builder()
			.imagePrompt(new ImagePrompt(List.of(new ImageMessage("you're a chimney sweep"),
					new ImageMessage("supercalifragilisticexpialidocious"))))
			.provider("superprovider")
			.requestOptions(ImageOptionsBuilder.builder().model("mistral").build())
			.build();
		var augmentedContext = this.observationFilter.map(originalContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues())
			.contains(KeyValue.of(AiObservationAttributes.PROMPT.value(),
					"[\"you're a chimney sweep\", \"supercalifragilisticexpialidocious\"]"));
	}

}
