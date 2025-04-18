package org.springframework.ai.chat.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import org.springframework.ai.observation.conventions.AiObservationAttributes;
import org.springframework.ai.observation.conventions.AiObservationEventNames;
import org.springframework.ai.observation.tracing.TracingHelper;

public class ChatModelCompletionObservationHandler implements ObservationHandler<ChatModelObservationContext> {

	@Override
	public void onStop(ChatModelObservationContext context) {
		TracingObservationHandler.TracingContext tracingContext = context
			.get(TracingObservationHandler.TracingContext.class);
		Span otelSpan = TracingHelper.extractOtelSpan(tracingContext);

		if (otelSpan != null) {
			otelSpan.addEvent(AiObservationEventNames.CONTENT_COMPLETION.value(),
					Attributes.of(AttributeKey.stringArrayKey(AiObservationAttributes.COMPLETION.value()),
							ChatModelObservationContentProcessor.completion(context)));
		}
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof ChatModelObservationContext;
	}

}
