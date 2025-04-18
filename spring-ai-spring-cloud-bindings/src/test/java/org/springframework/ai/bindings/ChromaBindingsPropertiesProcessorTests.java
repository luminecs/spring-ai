package org.springframework.ai.bindings;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ChromaBindingsPropertiesProcessorTests {

	private final Bindings bindings = new Bindings(new Binding("test-name", Paths.get("test-path"),
	// @formatter:off
			Map.of(
				Binding.TYPE, ChromaBindingsPropertiesProcessor.TYPE,
				"uri", "https://example.net:8000",
				"username", "itsme",
				"password", "youknowit"
			)));
	// @formatter:on

	private final MockEnvironment environment = new MockEnvironment();

	private final Map<String, Object> properties = new HashMap<>();

	@Test
	void propertiesAreContributed() {
		new ChromaBindingsPropertiesProcessor().process(this.environment, this.bindings, this.properties);
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.chroma.client.host", "https://example.net");
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.chroma.client.port", "8000");
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.chroma.client.username", "itsme");
		assertThat(this.properties).containsEntry("spring.ai.vectorstore.chroma.client.password", "youknowit");
	}

	@Test
	void whenDisabledThenPropertiesAreNotContributed() {
		this.environment.setProperty(
				"%s.chroma.enabled".formatted(org.springframework.ai.bindings.BindingsValidator.CONFIG_PATH), "false");

		new ChromaBindingsPropertiesProcessor().process(this.environment, this.bindings, this.properties);
		assertThat(this.properties).isEmpty();
	}

}
