package org.springframework.ai.azure.openai.aot;

import java.util.Set;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatChoice;
import org.junit.jupiter.api.Test;

import org.springframework.ai.aot.AiRuntimeHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.resource;

class AzureOpenAiRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		AzureOpenAiRuntimeHints openAiRuntimeHints = new AzureOpenAiRuntimeHints();
		openAiRuntimeHints.registerHints(runtimeHints, null);

		Set<TypeReference> azureModelTypes = AiRuntimeHints.findClassesInPackage(ChatChoice.class.getPackageName(),
				(metadataReader, metadataReaderFactory) -> true);
		for (TypeReference modelType : azureModelTypes) {
			assertThat(runtimeHints).matches(reflection().onType(modelType));
		}
		assertThat(runtimeHints).matches(reflection().onType(OpenAIClient.class));
		assertThat(runtimeHints).matches(reflection().onType(OpenAIAsyncClient.class));

		assertThat(runtimeHints).matches(resource().forResource("/azure-ai-openai.properties"));
	}

}
