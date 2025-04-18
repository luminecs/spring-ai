package org.springframework.ai.chat.observation;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

import org.springframework.ai.model.observation.ModelUsageMetricsGenerator;

public class ChatModelMeterObservationHandler implements ObservationHandler<ChatModelObservationContext> {

	private final MeterRegistry meterRegistry;

	public ChatModelMeterObservationHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void onStop(ChatModelObservationContext context) {
		if (context.getResponse() != null && context.getResponse().getMetadata() != null
				&& context.getResponse().getMetadata().getUsage() != null) {
			ModelUsageMetricsGenerator.generate(context.getResponse().getMetadata().getUsage(), context,
					this.meterRegistry);
		}
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof ChatModelObservationContext;
	}

}
