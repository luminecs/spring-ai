package org.springframework.ai.retry.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringAiRetryAutoConfigurationIT {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
			AutoConfigurations.of(SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class));

	@Test
	void testRetryAutoConfiguration() {
		this.contextRunner.run(context -> {
			assertThat(context).hasSingleBean(RetryTemplate.class);
			assertThat(context).hasSingleBean(ResponseErrorHandler.class);
		});
	}

}
