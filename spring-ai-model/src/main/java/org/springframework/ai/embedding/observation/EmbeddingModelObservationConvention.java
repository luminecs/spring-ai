package org.springframework.ai.embedding.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public interface EmbeddingModelObservationConvention extends ObservationConvention<EmbeddingModelObservationContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof EmbeddingModelObservationContext;
	}

}
