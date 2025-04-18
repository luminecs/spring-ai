package org.springframework.ai.retry.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringAiRetryPropertiesTests {

	@Test
	public void retryDefaultProperties() {

		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class))
			.run(context -> {
				var retryProperties = context.getBean(SpringAiRetryProperties.class);

				assertThat(retryProperties.getMaxAttempts()).isEqualTo(10);

				assertThat(retryProperties.isOnClientErrors()).isFalse();
				assertThat(retryProperties.getExcludeOnHttpCodes()).isEmpty();
				assertThat(retryProperties.getOnHttpCodes()).isEmpty();
				assertThat(retryProperties.getBackoff().getInitialInterval().toMillis()).isEqualTo(2000);
				assertThat(retryProperties.getBackoff().getMultiplier()).isEqualTo(5);
				assertThat(retryProperties.getBackoff().getMaxInterval().toMillis()).isEqualTo(3 * 60000);
			});
	}

	@Test
	public void retryCustomProperties() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
				"spring.ai.retry.max-attempts=100",
				"spring.ai.retry.on-client-errors=false",
				"spring.ai.retry.exclude-on-http-codes=404,500",
				"spring.ai.retry.on-http-codes=429",
				"spring.ai.retry.backoff.initial-interval=1000",
				"spring.ai.retry.backoff.multiplier=2",
				"spring.ai.retry.backoff.max-interval=60000")
				// @formatter:on
			.withConfiguration(AutoConfigurations.of(SpringAiRetryAutoConfiguration.class))
			.run(context -> {
				var retryProperties = context.getBean(SpringAiRetryProperties.class);

				assertThat(retryProperties.getMaxAttempts()).isEqualTo(100);
				assertThat(retryProperties.isOnClientErrors()).isFalse();
				assertThat(retryProperties.getExcludeOnHttpCodes()).containsExactly(404, 500);
				assertThat(retryProperties.getOnHttpCodes()).containsExactly(429);
				assertThat(retryProperties.getBackoff().getInitialInterval().toMillis()).isEqualTo(1000);
				assertThat(retryProperties.getBackoff().getMultiplier()).isEqualTo(2);
				assertThat(retryProperties.getBackoff().getMaxInterval().toMillis()).isEqualTo(60000);
			});
	}

}
