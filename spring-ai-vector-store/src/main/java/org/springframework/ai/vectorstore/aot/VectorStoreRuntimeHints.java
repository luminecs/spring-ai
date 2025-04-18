package org.springframework.ai.vectorstore.aot;

import java.util.Set;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class VectorStoreRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {

		for (var r : Set.of("antlr4/org/springframework/ai/vectorstore/filter/antlr4/Filters.g4")) {
			hints.resources().registerResource(new ClassPathResource(r));
		}
	}

}
