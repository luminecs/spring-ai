package org.springframework.ai.mcp.client.autoconfigure.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpSseClientPropertiesTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(TestConfiguration.class);

	@Test
	void defaultValues() {
		this.contextRunner.run(context -> {
			McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
			assertThat(properties.getConnections()).isNotNull();
			assertThat(properties.getConnections()).isEmpty();
		});
	}

	@Test
	void singleConnection() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080/events")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections()).containsKey("server1");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080/events");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isNull();
			});
	}

	@Test
	void multipleConnections() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080/events",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081/events")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(2);
				assertThat(properties.getConnections()).containsKeys("server1", "server2");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080/events");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isNull();
				assertThat(properties.getConnections().get("server2").url())
					.isEqualTo("http://otherserver:8081/events");
				assertThat(properties.getConnections().get("server2").sseEndpoint()).isNull();
			});
	}

	@Test
	void connectionWithEmptyUrl() {
		this.contextRunner.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=").run(context -> {
			McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
			assertThat(properties.getConnections()).hasSize(1);
			assertThat(properties.getConnections()).containsKey("server1");
			assertThat(properties.getConnections().get("server1").url()).isEmpty();
			assertThat(properties.getConnections().get("server1").sseEndpoint()).isNull();
		});
	}

	@Test
	void connectionWithNullUrl() {

		McpSseClientProperties properties = new McpSseClientProperties();
		Map<String, McpSseClientProperties.SseParameters> connections = properties.getConnections();

		assertThat(connections).isNotNull();
		assertThat(connections).isEmpty();
	}

	@Test
	void sseParametersRecord() {
		String url = "http://test-server:8080/events";
		String sseUrl = "/sse";
		McpSseClientProperties.SseParameters params = new McpSseClientProperties.SseParameters(url, sseUrl);

		assertThat(params.url()).isEqualTo(url);
		assertThat(params.sseEndpoint()).isEqualTo(sseUrl);
	}

	@Test
	void sseParametersRecordWithNullSseEdnpoint() {
		String url = "http://test-server:8080/events";
		McpSseClientProperties.SseParameters params = new McpSseClientProperties.SseParameters(url, null);

		assertThat(params.url()).isEqualTo(url);
		assertThat(params.sseEndpoint()).isNull();
	}

	@Test
	void configPrefixConstant() {
		assertThat(McpSseClientProperties.CONFIG_PREFIX).isEqualTo("spring.ai.mcp.client.sse");
	}

	@Test
	void yamlConfigurationBinding() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080/events",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081/events")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(2);
				assertThat(properties.getConnections()).containsKeys("server1", "server2");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080/events");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isNull();
				assertThat(properties.getConnections().get("server2").url())
					.isEqualTo("http://otherserver:8081/events");
				assertThat(properties.getConnections().get("server2").sseEndpoint()).isNull();
			});
	}

	@Test
	void connectionMapManipulation() {
		this.contextRunner.run(context -> {
			McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
			Map<String, McpSseClientProperties.SseParameters> connections = properties.getConnections();

			connections.put("server1",
					new McpSseClientProperties.SseParameters("http://localhost:8080/events", "/sse"));
			assertThat(properties.getConnections()).hasSize(1);
			assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080/events");
			assertThat(properties.getConnections().get("server1").sseEndpoint()).isEqualTo("/sse");

			connections.put("server2",
					new McpSseClientProperties.SseParameters("http://otherserver:8081/events", null));
			assertThat(properties.getConnections()).hasSize(2);
			assertThat(properties.getConnections().get("server2").url()).isEqualTo("http://otherserver:8081/events");
			assertThat(properties.getConnections().get("server2").sseEndpoint()).isNull();

			connections.put("server1",
					new McpSseClientProperties.SseParameters("http://newserver:8082/events", "/events"));
			assertThat(properties.getConnections()).hasSize(2);
			assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://newserver:8082/events");
			assertThat(properties.getConnections().get("server1").sseEndpoint()).isEqualTo("/events");

			connections.remove("server1");
			assertThat(properties.getConnections()).hasSize(1);
			assertThat(properties.getConnections()).containsKey("server2");
			assertThat(properties.getConnections()).doesNotContainKey("server1");
		});
	}

	@Test
	void specialCharactersInUrl() {
		this.contextRunner.withPropertyValues(
				"spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080/events?param=value&other=123")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections().get("server1").url())
					.isEqualTo("http://localhost:8080/events?param=value&other=123");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isNull();
			});
	}

	@Test
	void specialCharactersInConnectionName() {
		this.contextRunner
			.withPropertyValues(
					"spring.ai.mcp.client.sse.connections.server-with-dashes.url=http://localhost:8080/events")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections()).containsKey("server-with-dashes");
				assertThat(properties.getConnections().get("server-with-dashes").url())
					.isEqualTo("http://localhost:8080/events");
				assertThat(properties.getConnections().get("server-with-dashes").sseEndpoint()).isNull();
			});
	}

	@Test
	void connectionWithSseEndpoint() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/events")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections()).containsKey("server1");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isEqualTo("/events");
			});
	}

	@Test
	void multipleConnectionsWithSseEndpoint() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/events",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081",
					"spring.ai.mcp.client.sse.connections.server2.sse-endpoint=/sse")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(2);
				assertThat(properties.getConnections()).containsKeys("server1", "server2");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isEqualTo("/events");
				assertThat(properties.getConnections().get("server2").url()).isEqualTo("http://otherserver:8081");
				assertThat(properties.getConnections().get("server2").sseEndpoint()).isEqualTo("/sse");
			});
	}

	@Test
	void connectionWithEmptySseEndpoint() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections()).containsKey("server1");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isEmpty();
			});
	}

	@Test
	void mixedConnectionsWithAndWithoutSseEndpoint() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/events",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(2);
				assertThat(properties.getConnections()).containsKeys("server1", "server2");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080");
				assertThat(properties.getConnections().get("server1").sseEndpoint()).isEqualTo("/events");
				assertThat(properties.getConnections().get("server2").url()).isEqualTo("http://otherserver:8081");
				assertThat(properties.getConnections().get("server2").sseEndpoint()).isNull();
			});
	}

	@Test
	void specialCharactersInSseEndpoint() {
		this.contextRunner
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/events/stream?format=json&timeout=30")
			.run(context -> {
				McpSseClientProperties properties = context.getBean(McpSseClientProperties.class);
				assertThat(properties.getConnections()).hasSize(1);
				assertThat(properties.getConnections()).containsKey("server1");
				assertThat(properties.getConnections().get("server1").url()).isEqualTo("http://localhost:8080");
				assertThat(properties.getConnections().get("server1").sseEndpoint())
					.isEqualTo("/events/stream?format=json&timeout=30");
			});
	}

	@Configuration
	@EnableConfigurationProperties(McpSseClientProperties.class)
	static class TestConfiguration {

	}

}
