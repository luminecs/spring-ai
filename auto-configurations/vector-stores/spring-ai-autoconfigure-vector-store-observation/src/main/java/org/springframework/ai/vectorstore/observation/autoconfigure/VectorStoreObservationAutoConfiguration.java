package org.springframework.ai.vectorstore.observation.autoconfigure;

import io.micrometer.tracing.otel.bridge.OtelTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreQueryResponseObservationFilter;
import org.springframework.ai.vectorstore.observation.VectorStoreQueryResponseObservationHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration(
		afterName = { "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration" })
@ConditionalOnClass(VectorStore.class)
@EnableConfigurationProperties({ VectorStoreObservationProperties.class })
public class VectorStoreObservationAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(VectorStoreObservationAutoConfiguration.class);

	private static void logQueryResponseContentWarning() {
		logger.warn(
				"You have enabled the inclusion of the query response content in the observations, with the risk of exposing sensitive or private information. Please, be careful!");
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(OtelTracer.class)
	@ConditionalOnBean(OtelTracer.class)
	static class PrimaryVectorStoreQueryResponseContentObservationConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = VectorStoreObservationProperties.CONFIG_PREFIX, name = "include-query-response",
				havingValue = "true")
		VectorStoreQueryResponseObservationHandler vectorStoreQueryResponseObservationHandler() {
			logQueryResponseContentWarning();
			return new VectorStoreQueryResponseObservationHandler();
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("io.micrometer.tracing.otel.bridge.OtelTracer")
	static class FallbackVectorStoreQueryResponseContentObservationConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = VectorStoreObservationProperties.CONFIG_PREFIX, name = "include-query-response",
				havingValue = "true")
		VectorStoreQueryResponseObservationFilter vectorStoreQueryResponseContentObservationFilter() {
			logQueryResponseContentWarning();
			return new VectorStoreQueryResponseObservationFilter();
		}

	}

}
