package org.springframework.ai.vectorstore.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface VectorStoreObservationConvention extends ObservationConvention<VectorStoreObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof VectorStoreObservationContext;
	}

}
