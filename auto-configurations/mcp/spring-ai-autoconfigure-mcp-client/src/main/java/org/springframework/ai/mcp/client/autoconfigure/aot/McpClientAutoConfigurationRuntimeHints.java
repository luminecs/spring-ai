package org.springframework.ai.mcp.client.autoconfigure.aot;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class McpClientAutoConfigurationRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.resources().registerPattern("**.json");

		var mcs = MemberCategory.values();
		for (var tr : findJsonAnnotatedClassesInPackage("org.springframework.ai.mcp.client.autoconfigure")) {
			hints.reflection().registerType(tr, mcs);
		}
	}

}
