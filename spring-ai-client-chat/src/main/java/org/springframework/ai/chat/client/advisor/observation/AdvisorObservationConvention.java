package org.springframework.ai.chat.client.advisor.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface AdvisorObservationConvention extends ObservationConvention<AdvisorObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof AdvisorObservationContext;
	}

}
