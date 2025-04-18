package org.springframework.ai.moonshot.aot;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ai.moonshot.MoonshotChatOptions;
import org.springframework.ai.moonshot.api.MoonshotApi;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

class MoonshotRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		MoonshotRuntimeHints moonshotRuntimeHints = new MoonshotRuntimeHints();
		moonshotRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> jsonAnnotatedClasses = findJsonAnnotatedClassesInPackage("org.springframework.ai.moonshot");

		Set<TypeReference> registeredTypes = new HashSet<>();
		runtimeHints.reflection().typeHints().forEach(typeHint -> registeredTypes.add(typeHint.getType()));

		for (TypeReference jsonAnnotatedClass : jsonAnnotatedClasses) {
			assertThat(registeredTypes.contains(jsonAnnotatedClass)).isTrue();
		}

		assertThat(registeredTypes.contains(TypeReference.of(MoonshotApi.ChatCompletion.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(MoonshotApi.ChatCompletionRequest.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(MoonshotApi.ChatCompletionChunk.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(MoonshotApi.Usage.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(MoonshotChatOptions.class))).isTrue();
	}

}
