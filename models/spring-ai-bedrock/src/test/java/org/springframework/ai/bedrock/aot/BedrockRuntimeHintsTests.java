package org.springframework.ai.bedrock.aot;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ai.bedrock.api.AbstractBedrockApi;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingOptions;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingOptions;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

class BedrockRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		BedrockRuntimeHints bedrockRuntimeHints = new BedrockRuntimeHints();
		bedrockRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> jsonAnnotatedClasses = findJsonAnnotatedClassesInPackage("org.springframework.ai.bedrock");

		Set<TypeReference> registeredTypes = new HashSet<>();
		runtimeHints.reflection().typeHints().forEach(typeHint -> registeredTypes.add(typeHint.getType()));

		for (TypeReference jsonAnnotatedClass : jsonAnnotatedClasses) {
			assertThat(registeredTypes.contains(jsonAnnotatedClass)).isTrue();
		}

		assertThat(registeredTypes.contains(TypeReference.of(AbstractBedrockApi.AmazonBedrockInvocationMetrics.class)))
			.isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(CohereEmbeddingBedrockApi.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(BedrockCohereEmbeddingOptions.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(BedrockTitanEmbeddingOptions.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(TitanEmbeddingBedrockApi.class))).isTrue();
	}

}
