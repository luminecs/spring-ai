package org.springframework.ai.image.observation;

import java.util.StringJoiner;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import org.springframework.util.CollectionUtils;

public class ImageModelPromptContentObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {
		if (!(context instanceof ImageModelObservationContext imageModelObservationContext)) {
			return context;
		}

		if (CollectionUtils.isEmpty(imageModelObservationContext.getRequest().getInstructions())) {
			return imageModelObservationContext;
		}

		StringJoiner promptMessagesJoiner = new StringJoiner(", ", "[", "]");
		imageModelObservationContext.getRequest()
			.getInstructions()
			.forEach(message -> promptMessagesJoiner.add("\"" + message.getText() + "\""));

		imageModelObservationContext
			.addHighCardinalityKeyValue(ImageModelObservationDocumentation.HighCardinalityKeyNames.PROMPT
				.withValue(promptMessagesJoiner.toString()));

		return imageModelObservationContext;
	}

}
