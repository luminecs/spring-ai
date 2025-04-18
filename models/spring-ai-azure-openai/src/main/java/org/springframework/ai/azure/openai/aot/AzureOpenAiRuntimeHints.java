package org.springframework.ai.azure.openai.aot;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatChoice;

import org.springframework.ai.aot.AiRuntimeHints;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class AzureOpenAiRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {

		var mcs = MemberCategory.values();

		hints.reflection().registerType(OpenAIClient.class, mcs);
		hints.reflection().registerType(OpenAIAsyncClient.class, mcs);

		AiRuntimeHints
			.findClassesInPackage(ChatChoice.class.getPackageName(), (metadataReader, metadataReaderFactory) -> true)
			.forEach(clazz -> hints.reflection().registerType(clazz, mcs));

		hints.proxies().registerJdkProxy(com.azure.ai.openai.implementation.OpenAIClientImpl.OpenAIClientService.class);

		try {
			var resolver = new PathMatchingResourcePatternResolver();
			for (var resourceMatch : resolver.getResources("/azure-ai-openai.properties")) {
				hints.resources().registerResource(resourceMatch);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
