package org.springframework.ai.aot;

import org.junit.jupiter.api.Test;

import org.springframework.ai.model.function.FunctionCallback;
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
		assertThat(runtimeHints).matches(resource().forResource("embedding/embedding-model-dimensions.properties"));

		assertThat(runtimeHints).matches(reflection().onMethod(FunctionCallback.class, "getDescription"));
		assertThat(runtimeHints).matches(reflection().onMethod(FunctionCallback.class, "getInputTypeSchema"));
		assertThat(runtimeHints).matches(reflection().onMethod(FunctionCallback.class, "getName"));
	}

}
