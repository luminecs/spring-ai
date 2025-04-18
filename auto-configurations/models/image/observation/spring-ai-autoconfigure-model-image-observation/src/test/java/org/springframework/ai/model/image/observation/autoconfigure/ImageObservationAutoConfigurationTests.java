package org.springframework.ai.model.image.observation.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.image.observation.ImageModelPromptContentObservationFilter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ImageObservationAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ImageObservationAutoConfiguration.class));

	@Test
	void promptFilterDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ImageModelPromptContentObservationFilter.class));
	}

	@Test
	void promptFilterEnabled() {
		this.contextRunner.withPropertyValues("spring.ai.image.observations.include-prompt=true")
			.run(context -> assertThat(context).hasSingleBean(ImageModelPromptContentObservationFilter.class));
	}

}
