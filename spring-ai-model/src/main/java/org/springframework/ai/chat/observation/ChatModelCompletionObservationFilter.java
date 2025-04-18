package org.springframework.ai.chat.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import org.springframework.ai.observation.tracing.TracingHelper;

public class ChatModelCompletionObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {
		if (!(context instanceof ChatModelObservationContext chatModelObservationContext)) {
			return context;
		}

		var completions = ChatModelObservationContentProcessor.completion(chatModelObservationContext);

		chatModelObservationContext
			.addHighCardinalityKeyValue(ChatModelObservationDocumentation.HighCardinalityKeyNames.COMPLETION
				.withValue(TracingHelper.concatenateStrings(completions)));

		return chatModelObservationContext;
	}

}
