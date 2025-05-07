package org.springframework.ai.template;

import java.util.Map;
import java.util.function.BiFunction;

public interface TemplateRenderer extends BiFunction<String, Map<String, Object>, String> {

	@Override
	String apply(String template, Map<String, Object> variables);

}
