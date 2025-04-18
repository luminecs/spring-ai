package org.springframework.ai.bindings;

import org.springframework.core.env.Environment;

final class BindingsValidator {

	static final String CONFIG_PATH = "spring.ai.cloud.bindings";

	private BindingsValidator() {

	}

	static boolean isTypeEnabled(Environment environment, String type) {
		return environment.getProperty("%s.%s.enabled".formatted(CONFIG_PATH, type), Boolean.class, true);
	}

}
