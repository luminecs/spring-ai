package org.springframework.ai.bedrock.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class BedrockRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		var mcs = MemberCategory.values();

		for (var tr : findJsonAnnotatedClassesInPackage("org.springframework.ai.bedrock")) {
			hints.reflection().registerType(tr, mcs);
		}
	}

}
