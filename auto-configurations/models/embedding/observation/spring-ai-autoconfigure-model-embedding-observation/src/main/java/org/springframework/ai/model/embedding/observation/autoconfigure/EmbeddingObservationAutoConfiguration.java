package org.springframework.ai.model.embedding.observation.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelMeterObservationHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
		afterName = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration")
@ConditionalOnClass(EmbeddingModel.class)
public class EmbeddingObservationAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(MeterRegistry.class)
	EmbeddingModelMeterObservationHandler embeddingModelMeterObservationHandler(
			ObjectProvider<MeterRegistry> meterRegistry) {
		return new EmbeddingModelMeterObservationHandler(meterRegistry.getObject());
	}

}
