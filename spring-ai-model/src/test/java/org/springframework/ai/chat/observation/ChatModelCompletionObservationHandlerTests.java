package org.springframework.ai.chat.observation;

import java.util.List;

import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.observation.conventions.AiObservationAttributes;
import org.springframework.ai.observation.conventions.AiObservationEventNames;
import org.springframework.ai.observation.tracing.TracingHelper;

import static org.assertj.core.api.Assertions.assertThat;

class ChatModelCompletionObservationHandlerTests {

	@Test
	void whenCompletionWithTextThenSpanEvent() {
		var observationContext = ChatModelObservationContext.builder()
			.prompt(new Prompt("supercalifragilisticexpialidocious",
					ChatOptions.builder().model("spoonful-of-sugar").build()))
			.provider("mary-poppins")
			.build();
		observationContext.setResponse(new ChatResponse(List.of(new Generation(new AssistantMessage("say please")),
				new Generation(new AssistantMessage("seriously, say please")))));
		var sdkTracer = SdkTracerProvider.builder().build().get("test");
		var otelTracer = new OtelTracer(sdkTracer, new OtelCurrentTraceContext(), null);
		var span = otelTracer.nextSpan();
		var tracingContext = new TracingObservationHandler.TracingContext();
		tracingContext.setSpan(span);
		observationContext.put(TracingObservationHandler.TracingContext.class, tracingContext);

		new ChatModelCompletionObservationHandler().onStop(observationContext);

		var otelSpan = TracingHelper.extractOtelSpan(tracingContext);
		assertThat(otelSpan).isNotNull();
		var spanData = ((ReadableSpan) otelSpan).toSpanData();
		assertThat(spanData.getEvents().size()).isEqualTo(1);
		assertThat(spanData.getEvents().get(0).getName()).isEqualTo(AiObservationEventNames.CONTENT_COMPLETION.value());
		assertThat(spanData.getEvents()
			.get(0)
			.getAttributes()
			.get(AttributeKey.stringArrayKey(AiObservationAttributes.COMPLETION.value())))
			.containsOnly("say please", "seriously, say please");
	}

}
