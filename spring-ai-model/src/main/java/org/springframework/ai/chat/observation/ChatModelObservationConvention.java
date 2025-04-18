package org.springframework.ai.chat.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface ChatModelObservationConvention extends ObservationConvention<ChatModelObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof ChatModelObservationContext;
	}

}
