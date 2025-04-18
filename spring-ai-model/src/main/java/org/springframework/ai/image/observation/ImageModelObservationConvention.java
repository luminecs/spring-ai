package org.springframework.ai.image.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface ImageModelObservationConvention extends ObservationConvention<ImageModelObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof ImageModelObservationContext;
	}

}
