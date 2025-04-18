package org.springframework.ai.mcp.client.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class McpToolCallbackAutoConfigurationTests {

	private final ApplicationContextRunner applicationContext = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(McpToolCallbackAutoConfiguration.class));

	@Test
	void disabledByDeafault() {

		this.applicationContext.run(context -> {
			assertThat(context).doesNotHaveBean("mcpToolCallbacks");
			assertThat(context).doesNotHaveBean("mcpAsyncToolCallbacks");
		});

		this.applicationContext
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.type=SYNC")
			.run(context -> {
				assertThat(context).doesNotHaveBean("mcpToolCallbacks");
				assertThat(context).doesNotHaveBean("mcpAsyncToolCallbacks");
			});

		this.applicationContext
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.type=ASYNC")
			.run(context -> {
				assertThat(context).doesNotHaveBean("mcpToolCallbacks");
				assertThat(context).doesNotHaveBean("mcpAsyncToolCallbacks");
			});
	}

	@Test
	void enabledMcpToolCallbackAutoconfiguration() {

		this.applicationContext.withPropertyValues("spring.ai.mcp.client.toolcallback.enabled=true").run(context -> {
			assertThat(context).hasBean("mcpToolCallbacks");
			assertThat(context).doesNotHaveBean("mcpAsyncToolCallbacks");
		});

		this.applicationContext
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.toolcallback.enabled=true",
					"spring.ai.mcp.client.type=SYNC")
			.run(context -> {
				assertThat(context).hasBean("mcpToolCallbacks");
				assertThat(context).doesNotHaveBean("mcpAsyncToolCallbacks");
			});

		this.applicationContext
			.withPropertyValues("spring.ai.mcp.client.toolcallback.enabled=true", "spring.ai.mcp.client.type=ASYNC")
			.run(context -> {
				assertThat(context).doesNotHaveBean("mcpToolCallbacks");
				assertThat(context).hasBean("mcpAsyncToolCallbacks");
			});

		this.applicationContext
			.withPropertyValues("spring.ai.mcp.client.enabled=true", "spring.ai.mcp.client.toolcallback.enabled=true",
					"spring.ai.mcp.client.type=ASYNC")
			.run(context -> {
				assertThat(context).doesNotHaveBean("mcpToolCallbacks");
				assertThat(context).hasBean("mcpAsyncToolCallbacks");
			});
	}

}
