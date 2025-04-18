package org.springframework.ai.bindings;

import java.util.Arrays;
import java.util.Map;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

public class TanzuBindingsPropertiesProcessor implements BindingsPropertiesProcessor {

	public static final String TYPE = "genai";

	@Override
	public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
		if (!BindingsValidator.isTypeEnabled(environment, TYPE)) {
			return;
		}

		bindings.filterBindings(TYPE).forEach(binding -> {
			if (binding.getSecret().get("model-capabilities") != null) {
				String[] capabilities = binding.getSecret().get("model-capabilities").trim().split("\\s*,\\s*");
				if (Arrays.stream(capabilities).anyMatch("chat"::equals)) {
					properties.put("spring.ai.openai.chat.api-key", binding.getSecret().get("api-key"));
					properties.put("spring.ai.openai.chat.base-url", binding.getSecret().get("uri"));
					properties.put("spring.ai.openai.chat.options.model", binding.getSecret().get("model-name"));
				}
				if (Arrays.stream(capabilities).anyMatch("embedding"::equals)) {
					properties.put("spring.ai.openai.embedding.api-key", binding.getSecret().get("api-key"));
					properties.put("spring.ai.openai.embedding.base-url", binding.getSecret().get("uri"));
					properties.put("spring.ai.openai.embedding.options.model", binding.getSecret().get("model-name"));
				}
			}
		});
	}

}
