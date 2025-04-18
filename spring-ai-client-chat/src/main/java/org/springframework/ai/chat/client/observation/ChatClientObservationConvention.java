package org.springframework.ai.chat.client.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface ChatClientObservationConvention extends ObservationConvention<ChatClientObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof ChatClientObservationContext;
	}

}
