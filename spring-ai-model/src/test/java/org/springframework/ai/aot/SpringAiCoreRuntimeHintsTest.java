package org.springframework.ai.aot;

import org.junit.jupiter.api.Test;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.aot.hint.RuntimeHints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.resource;

class SpringAiCoreRuntimeHintsTest {

	@Test
	void core() {
		var runtimeHints = new RuntimeHints();
		var springAiCore = new SpringAiCoreRuntimeHints();
		springAiCore.registerHints(runtimeHints, null);

		// Verify resource hints
		assertThat(runtimeHints).matches(resource().forResource("embedding/embedding-model-dimensions.properties"));

		// Verify ToolCallback and ToolDefinition type registration
		assertThat(runtimeHints).matches(reflection().onType(ToolCallback.class));
		assertThat(runtimeHints).matches(reflection().onType(ToolDefinition.class));
	}

}