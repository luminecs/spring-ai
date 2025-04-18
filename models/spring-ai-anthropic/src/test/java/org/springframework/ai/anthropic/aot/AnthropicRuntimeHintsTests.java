package org.springframework.ai.anthropic.aot;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

class AnthropicRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		AnthropicRuntimeHints anthropicRuntimeHints = new AnthropicRuntimeHints();
		anthropicRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> jsonAnnotatedClasses = findJsonAnnotatedClassesInPackage("org.springframework.ai.anthropic");

		Set<TypeReference> registeredTypes = new HashSet<>();
		runtimeHints.reflection().typeHints().forEach(typeHint -> registeredTypes.add(typeHint.getType()));

		for (TypeReference jsonAnnotatedClass : jsonAnnotatedClasses) {
			assertThat(registeredTypes.contains(jsonAnnotatedClass)).isTrue();
		}

		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.Role.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.ThinkingType.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.EventType.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.ContentBlock.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.ChatCompletionRequest.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(AnthropicApi.AnthropicMessage.class))).isTrue();
	}

}
