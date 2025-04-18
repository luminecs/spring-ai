package org.springframework.ai.model.observation;

import java.util.List;
import java.util.function.Consumer;

import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;

@SuppressWarnings({ "rawtypes", "null" })
public class ErrorLoggingObservationHandler implements ObservationHandler {

	private static final Logger logger = LoggerFactory.getLogger(ErrorLoggingObservationHandler.class);

	private final Tracer tracer;

	private final List<Class<? extends Observation.Context>> supportedContextTypes;

	private final Consumer<Context> errorConsumer;

	public ErrorLoggingObservationHandler(Tracer tracer,
			List<Class<? extends Observation.Context>> supportedContextTypes) {
		this(tracer, supportedContextTypes, context -> logger.error("Traced Error: ", context.getError()));
	}

	public ErrorLoggingObservationHandler(Tracer tracer,
			List<Class<? extends Observation.Context>> supportedContextTypes, Consumer<Context> errorConsumer) {

		Assert.notNull(tracer, "Tracer must not be null");
		Assert.notNull(supportedContextTypes, "SupportedContextTypes must not be null");
		Assert.notNull(errorConsumer, "ErrorConsumer must not be null");

		this.tracer = tracer;
		this.supportedContextTypes = supportedContextTypes;
		this.errorConsumer = errorConsumer;
	}

	@Override
	public boolean supportsContext(Context context) {
		return (context == null) ? false : this.supportedContextTypes.stream().anyMatch(clz -> clz.isInstance(context));
	}

	@Override
	public void onError(Context context) {
		if (context != null) {
			TracingContext tracingContext = context.get(TracingContext.class);
			if (tracingContext != null) {
				try (var val = this.tracer.withSpan(tracingContext.getSpan())) {
					this.errorConsumer.accept(context);
				}
			}
		}
	}

}
