package org.springframework.ai.mcp.aot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.lang.Nullable;

@SuppressWarnings("unused")
public class McpHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		var mcs = MemberCategory.values();

		for (var tr : innerClasses(McpSchema.class)) {
			hints.reflection().registerType(tr, mcs);
		}
	}

	private Set<TypeReference> innerClasses(Class<?> clazz) {
		var indent = new HashSet<String>();
		this.findNestedClasses(clazz, indent);
		return indent.stream().map(TypeReference::of).collect(Collectors.toSet());
	}

	private void findNestedClasses(Class<?> clazz, Set<String> indent) {
		var classes = new ArrayList<Class<?>>();
		classes.addAll(Arrays.asList(clazz.getDeclaredClasses()));
		classes.addAll(Arrays.asList(clazz.getClasses()));
		for (var nestedClass : classes) {
			this.findNestedClasses(nestedClass, indent);
		}
		indent.addAll(classes.stream().map(Class::getName).toList());
	}

}
