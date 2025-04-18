package org.springframework.ai.vectorstore.observation;

import java.util.List;

import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.observation.conventions.VectorStoreObservationAttributes;
import org.springframework.ai.observation.conventions.VectorStoreObservationEventNames;
import org.springframework.ai.observation.tracing.TracingHelper;

import static org.assertj.core.api.Assertions.assertThat;

class VectorStoreQueryResponseObservationHandlerTests {

	@Test
	void whenCompletionWithTextThenSpanEvent() {
		var observationContext = VectorStoreObservationContext
			.builder("db", VectorStoreObservationContext.Operation.ADD)
			.queryResponse(List.of(new Document("hello"), new Document("other-side")))
			.build();
		var sdkTracer = SdkTracerProvider.builder().build().get("test");
		var otelTracer = new OtelTracer(sdkTracer, new OtelCurrentTraceContext(), null);
		var span = otelTracer.nextSpan();
		var tracingContext = new TracingObservationHandler.TracingContext();
		tracingContext.setSpan(span);
		observationContext.put(TracingObservationHandler.TracingContext.class, tracingContext);

		new VectorStoreQueryResponseObservationHandler().onStop(observationContext);

		var otelSpan = TracingHelper.extractOtelSpan(tracingContext);
		assertThat(otelSpan).isNotNull();
		var spanData = ((ReadableSpan) otelSpan).toSpanData();
		assertThat(spanData.getEvents().size()).isEqualTo(1);
		assertThat(spanData.getEvents().get(0).getName())
			.isEqualTo(VectorStoreObservationEventNames.CONTENT_QUERY_RESPONSE.value());
		assertThat(spanData.getEvents()
			.get(0)
			.getAttributes()
			.get(AttributeKey.stringArrayKey(VectorStoreObservationAttributes.DB_VECTOR_QUERY_CONTENT.value())))
			.containsOnly("hello", "other-side");
	}

}
