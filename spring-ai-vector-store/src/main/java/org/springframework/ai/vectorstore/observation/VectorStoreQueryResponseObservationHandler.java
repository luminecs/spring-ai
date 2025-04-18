package org.springframework.ai.vectorstore.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import org.springframework.ai.observation.conventions.VectorStoreObservationAttributes;
import org.springframework.ai.observation.conventions.VectorStoreObservationEventNames;
import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.util.CollectionUtils;

public class VectorStoreQueryResponseObservationHandler implements ObservationHandler<VectorStoreObservationContext> {

	@Override
	public void onStop(VectorStoreObservationContext context) {
		TracingObservationHandler.TracingContext tracingContext = context
			.get(TracingObservationHandler.TracingContext.class);
		Span otelSpan = TracingHelper.extractOtelSpan(tracingContext);

		var documents = VectorStoreObservationContentProcessor.documents(context);

		if (!CollectionUtils.isEmpty(documents) && otelSpan != null) {
			otelSpan.addEvent(VectorStoreObservationEventNames.CONTENT_QUERY_RESPONSE.value(), Attributes.of(
					AttributeKey.stringArrayKey(VectorStoreObservationAttributes.DB_VECTOR_QUERY_CONTENT.value()),
					documents));
		}
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof VectorStoreObservationContext;
	}

}
