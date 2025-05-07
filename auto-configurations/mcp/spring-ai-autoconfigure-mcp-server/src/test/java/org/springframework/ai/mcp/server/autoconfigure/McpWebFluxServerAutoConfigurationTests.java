package org.springframework.ai.mcp.server.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;

class McpWebFluxServerAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(McpWebFluxServerAutoConfiguration.class,
				JacksonAutoConfiguration.class, TestConfiguration.class));

	@Test
	void shouldConfigureWebFluxTransportWithCustomObjectMapper() {
		this.contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(WebFluxSseServerTransportProvider.class);
			assertThat(context).hasSingleBean(RouterFunction.class);
			assertThat(context).hasSingleBean(McpServerProperties.class);

			ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

			assertThat(objectMapper.getDeserializationConfig()
				.isEnabled(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();

			String jsonWithUnknownField = """
					{
					    "tools": ["tool1", "tool2"],
					    "name": "test",
					    "unknownField": "value"
					}
					""";

			TestMessage message = objectMapper.readValue(jsonWithUnknownField, TestMessage.class);
			assertThat(message.getName()).isEqualTo("test");
		});
	}

	@Configuration
	@EnableConfigurationProperties(McpServerProperties.class)
	static class TestConfiguration {

	}

	static class TestMessage {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
