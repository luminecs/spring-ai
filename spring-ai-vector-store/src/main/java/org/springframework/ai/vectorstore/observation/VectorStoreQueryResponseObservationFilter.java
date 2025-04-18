package org.springframework.ai.vectorstore.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.util.CollectionUtils;

public class VectorStoreQueryResponseObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {

		if (!(context instanceof VectorStoreObservationContext observationContext)) {
			return context;
		}

		var documents = VectorStoreObservationContentProcessor.documents(observationContext);

		if (!CollectionUtils.isEmpty(documents)) {
			observationContext.addHighCardinalityKeyValue(
					VectorStoreObservationDocumentation.HighCardinalityKeyNames.DB_VECTOR_QUERY_RESPONSE_DOCUMENTS
						.withValue(TracingHelper.concatenateStrings(documents)));
		}

		return observationContext;
	}

}
