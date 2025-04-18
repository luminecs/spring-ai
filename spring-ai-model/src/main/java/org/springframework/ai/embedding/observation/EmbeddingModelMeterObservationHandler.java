package org.springframework.ai.embedding.observation;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;

import org.springframework.ai.model.observation.ModelUsageMetricsGenerator;

public class EmbeddingModelMeterObservationHandler implements ObservationHandler<EmbeddingModelObservationContext> {

	private final MeterRegistry meterRegistry;

	public EmbeddingModelMeterObservationHandler(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void onStop(EmbeddingModelObservationContext context) {
		if (context.getResponse() != null && context.getResponse().getMetadata() != null
				&& context.getResponse().getMetadata().getUsage() != null) {
			ModelUsageMetricsGenerator.generate(context.getResponse().getMetadata().getUsage(), context,
					this.meterRegistry);
		}
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof EmbeddingModelObservationContext;
	}

}
