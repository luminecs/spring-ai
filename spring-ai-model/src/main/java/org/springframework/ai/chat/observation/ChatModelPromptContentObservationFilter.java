package org.springframework.ai.chat.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import org.springframework.ai.observation.tracing.TracingHelper;

public class ChatModelPromptContentObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {
		if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
			return context;
		}

		var prompts = ChatModelObservationContentProcessor.prompt(chatModelObservationContext);

		chatModelObservationContext
			.addHighCardinalityKeyValue(ChatModelObservationDocumentation.HighCardinalityKeyNames.PROMPT
				.withValue(TracingHelper.concatenateStrings(prompts)));

		return chatModelObservationContext;
	}

}
