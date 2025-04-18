package org.springframework.ai.bindings;

import java.util.Map;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

public class OpenAiBindingsPropertiesProcessor implements BindingsPropertiesProcessor {

	public static final String TYPE = "openai";

	@Override
	public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
		if (!BindingsValidator.isTypeEnabled(environment, TYPE)) {
			return;
		}

		bindings.filterBindings(TYPE).forEach(binding -> {
			properties.put("spring.ai.openai.api-key", binding.getSecret().get("api-key"));
			properties.put("spring.ai.openai.base-url", binding.getSecret().get("uri"));
		});
	}

}
