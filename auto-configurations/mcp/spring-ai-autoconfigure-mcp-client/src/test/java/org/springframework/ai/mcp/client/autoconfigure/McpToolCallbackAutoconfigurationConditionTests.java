package org.springframework.ai.mcp.client.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.mcp.client.autoconfigure.McpToolCallbackAutoConfiguration.McpToolCallbackAutoconfigurationCondition;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class McpToolCallbackAutoconfigurationConditionTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(TestConfiguration.class);

	@Test
	void matchesWhenBothPropertiesAreEnabled() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.toolcallback.enabled=true")
			.run(context -> assertThat(context).hasBean("testBean"));
	}

	@Test
	void doesNotMatchWhenMcpClientIsDisabled() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.enabled=false", "spring.ai.mcp.client.toolcallback.enabled=true")
			.run(context -> assertThat(context).doesNotHaveBean("testBean"));
	}

	@Test
	void doesNotMatchWhenToolCallbackIsDisabled() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.toolcallback.enabled=false")
			.run(context -> assertThat(context).doesNotHaveBean("testBean"));
	}

	@Test
	void doesNotMatchWhenBothPropertiesAreDisabled() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.enabled=false", "spring.ai.mcp.client.toolcallback.enabled=false")
			.run(context -> assertThat(context).doesNotHaveBean("testBean"));
	}

	@Test
	void doesNotMatchWhenToolCallbackPropertyIsMissing() {

		this.contextRunner.withPropertyValues("spring.ai.mcp.client.enabled=true")
			.run(context -> assertThat(context).doesNotHaveBean("testBean"));
	}

	@Test
	void doesNotMatchWhenBothPropertiesAreMissing() {

		this.contextRunner.run(context -> assertThat(context).doesNotHaveBean("testBean"));
	}

	@Configuration
	@Conditional(McpToolCallbackAutoconfigurationCondition.class)
	static class TestConfiguration {

		@Bean
		String testBean() {
			return "testBean";
		}

	}

}
