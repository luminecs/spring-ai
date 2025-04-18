package org.springframework.ai.aot;

import org.junit.jupiter.api.Test;

import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.aot.hint.RuntimeHints;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

class ToolRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		ToolRuntimeHints toolRuntimeHints = new ToolRuntimeHints();
		toolRuntimeHints.registerHints(runtimeHints, null);
		assertThat(runtimeHints).matches(reflection().onType(DefaultToolCallResultConverter.class));
	}

}
