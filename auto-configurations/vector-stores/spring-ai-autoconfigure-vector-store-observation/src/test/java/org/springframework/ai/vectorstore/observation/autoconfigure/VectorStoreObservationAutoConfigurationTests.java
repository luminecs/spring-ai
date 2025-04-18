package org.springframework.ai.vectorstore.observation.autoconfigure;

import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.observation.VectorStoreQueryResponseObservationFilter;
import org.springframework.ai.vectorstore.observation.VectorStoreQueryResponseObservationHandler;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VectorStoreObservationAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(VectorStoreObservationAutoConfiguration.class));

	@Test
	void queryResponseFilterDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(VectorStoreQueryResponseObservationFilter.class));
	}

	@Test
	void queryResponseHandlerDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(VectorStoreQueryResponseObservationHandler.class));
	}

	@Test
	void queryResponseHandlerEnabled() {
		this.contextRunner
			.withBean(OtelTracer.class, OpenTelemetry.noop().getTracer("test"), new OtelCurrentTraceContext(), null)
			.withPropertyValues("spring.ai.vectorstore.observations.include-query-response=true")
			.run(context -> assertThat(context).hasSingleBean(VectorStoreQueryResponseObservationHandler.class));
	}

}
