package org.springframework.ai.model.chat.observation.autoconfigure;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.observation.ChatModelCompletionObservationFilter;
import org.springframework.ai.chat.observation.ChatModelCompletionObservationHandler;
import org.springframework.ai.chat.observation.ChatModelMeterObservationHandler;
import org.springframework.ai.chat.observation.ChatModelPromptContentObservationFilter;
import org.springframework.ai.chat.observation.ChatModelPromptContentObservationHandler;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ChatObservationAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ChatObservationAutoConfiguration.class));

	@Test
	void meterObservationHandlerEnabled() {
		this.contextRunner.withBean(CompositeMeterRegistry.class)
			.run(context -> assertThat(context).hasSingleBean(ChatModelMeterObservationHandler.class));
	}

	@Test
	void meterObservationHandlerDisabled() {
		this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(ChatModelMeterObservationHandler.class));
	}

	@Test
	void promptFilterDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelPromptContentObservationFilter.class));
	}

	@Test
	void promptHandlerDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelPromptContentObservationHandler.class));
	}

	@Test
	void promptHandlerEnabled() {
		this.contextRunner
			.withBean(OtelTracer.class, OpenTelemetry.noop().getTracer("test"), new OtelCurrentTraceContext(), null)
			.withPropertyValues("spring.ai.chat.observations.include-prompt=true")
			.run(context -> assertThat(context).hasSingleBean(ChatModelPromptContentObservationHandler.class));
	}

	@Test
	void promptHandlerDisabled() {
		this.contextRunner.withPropertyValues("spring.ai.chat.observations.include-prompt=true")
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelPromptContentObservationHandler.class));
	}

	@Test
	void completionFilterDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelCompletionObservationFilter.class));
	}

	@Test
	void completionHandlerDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelCompletionObservationHandler.class));
	}

	@Test
	void completionHandlerEnabled() {
		this.contextRunner
			.withBean(OtelTracer.class, OpenTelemetry.noop().getTracer("test"), new OtelCurrentTraceContext(), null)
			.withPropertyValues("spring.ai.chat.observations.include-completion=true")
			.run(context -> assertThat(context).hasSingleBean(ChatModelCompletionObservationHandler.class));
	}

	@Test
	void completionHandlerDisabled() {
		this.contextRunner.withPropertyValues("spring.ai.chat.observations.include-completion=true")
			.run(context -> assertThat(context).doesNotHaveBean(ChatModelCompletionObservationHandler.class));
	}

}
