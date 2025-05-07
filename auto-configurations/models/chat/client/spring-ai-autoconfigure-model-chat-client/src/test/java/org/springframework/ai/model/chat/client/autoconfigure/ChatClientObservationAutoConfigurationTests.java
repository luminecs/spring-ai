package org.springframework.ai.model.chat.client.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.observation.ChatClientPromptContentObservationFilter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ChatClientObservationAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ChatClientAutoConfiguration.class));

	@Test
	void promptContentFilterDefault() {
		this.contextRunner
			.run(context -> assertThat(context).doesNotHaveBean(ChatClientPromptContentObservationFilter.class));
	}

	@Test
	void promptContentFilterEnabled() {
		this.contextRunner.withPropertyValues("spring.ai.chat.client.observations.include-prompt=true")
			.run(context -> assertThat(context).hasSingleBean(ChatClientPromptContentObservationFilter.class));
	}

}
