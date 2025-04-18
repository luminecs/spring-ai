package org.springframework.ai.openai.aot;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

class OpenAiRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		OpenAiRuntimeHints openAiRuntimeHints = new OpenAiRuntimeHints();
		openAiRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> jsonAnnotatedClasses = findJsonAnnotatedClassesInPackage("org.springframework.ai.openai");

		Set<TypeReference> registeredTypes = new HashSet<>();
		runtimeHints.reflection().typeHints().forEach(typeHint -> registeredTypes.add(typeHint.getType()));

		for (TypeReference jsonAnnotatedClass : jsonAnnotatedClasses) {
			assertThat(registeredTypes.contains(jsonAnnotatedClass)).isTrue();
		}

		assertThat(registeredTypes.contains(TypeReference.of(OpenAiApi.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiAudioApi.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiAudioApi.TtsModel.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiAudioApi.WhisperModel.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiImageApi.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiApi.ChatCompletionFinishReason.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiApi.FunctionTool.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiApi.FunctionTool.Function.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiApi.OutputModality.class))).isTrue();
		assertThat(registeredTypes.contains(TypeReference.of(OpenAiChatOptions.class))).isTrue();
	}

}
