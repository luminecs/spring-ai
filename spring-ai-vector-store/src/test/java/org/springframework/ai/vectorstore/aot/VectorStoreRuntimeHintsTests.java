package org.springframework.ai.vectorstore.aot;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.resource;

public class VectorStoreRuntimeHintsTests {

	@Test
	void vectorStoreRuntimeHints() {
		var runtimeHints = new RuntimeHints();
		var vectorStoreHints = new VectorStoreRuntimeHints();
		vectorStoreHints.registerHints(runtimeHints, null);
		assertThat(runtimeHints)
			.matches(resource().forResource("antlr4/org/springframework/ai/vectorstore/filter/antlr4/Filters.g4"));
	}

}
