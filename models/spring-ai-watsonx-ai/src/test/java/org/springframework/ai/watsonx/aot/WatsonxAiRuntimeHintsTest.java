package org.springframework.ai.watsonx.aot;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ai.watsonx.api.WatsonxAiChatRequest;
import org.springframework.ai.watsonx.api.WatsonxAiChatResponse;
import org.springframework.ai.watsonx.api.WatsonxAiChatResults;
import org.springframework.ai.watsonx.api.WatsonxAiEmbeddingRequest;
import org.springframework.ai.watsonx.api.WatsonxAiEmbeddingResponse;
import org.springframework.ai.watsonx.api.WatsonxAiEmbeddingResults;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class WatsonxAiRuntimeHintsTest {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		WatsonxAiRuntimeHints watsonxAiRuntimeHints = new WatsonxAiRuntimeHints();
		watsonxAiRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> jsonAnnotatedClasses = findJsonAnnotatedClassesInPackage("org.springframework.ai.watsonx");

		Set<TypeReference> registeredTypes = new HashSet<>();
		runtimeHints.reflection().typeHints().forEach(typeHint -> registeredTypes.add(typeHint.getType()));

		for (TypeReference jsonAnnotatedClass : jsonAnnotatedClasses) {
			assertThat(registeredTypes.contains(jsonAnnotatedClass)).isTrue();
		}

		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiChatRequest.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiChatResponse.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiChatResults.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiEmbeddingRequest.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiEmbeddingResponse.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(WatsonxAiEmbeddingResults.class))).isTrue();
	}

}
