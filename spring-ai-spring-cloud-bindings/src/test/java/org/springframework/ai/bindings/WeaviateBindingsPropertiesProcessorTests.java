package org.springframework.ai.bindings;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class WeaviateBindingsPropertiesProcessorTests {

	private final Bindings bindings = new Bindings(new Binding("test-name", Paths.get("test-path"),
	// @formatter:off
			Map.of(
				Binding.TYPE, WeaviateBindingsPropertiesProcessor.TYPE,
				"uri", "https://example.net:8000",
				"api-key", "demo"
			)));
    // @formatter:on

	private final MockEnvironment environment = new MockEnvironment();

	private final Map<String, Object> properties = new HashMap<>();

	@Test
	void propertiesAreContributed() {
		new WeaviateBindingsPropertiesProcessor().process(this.environment, this.bindings, this.properties);
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.weaviate.scheme", "https");
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.weaviate.host", "example.net:8000");
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.weaviate.api-key", "demo");
	}

	@Test
	void whenDisabledThenPropertiesAreNotContributed() {
		this.environment.setProperty(
				"%s.weaviate.enabled".formatted(org.springframework.ai.bindings.BindingsValidator.CONFIG_PATH),
				"false");

		new WeaviateBindingsPropertiesProcessor().process(this.environment, this.bindings, this.properties);
		assertThat(this.properties).isEmpty();
	}

}
