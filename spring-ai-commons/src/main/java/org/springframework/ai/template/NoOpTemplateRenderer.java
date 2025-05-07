package org.springframework.ai.template;

import java.util.Map;

import org.springframework.util.Assert;

public class NoOpTemplateRenderer implements TemplateRenderer {

	@Override
	public String apply(String template, Map<String, Object> variables) {
		Assert.hasText(template, "template cannot be null or empty");
		Assert.notNull(variables, "variables cannot be null");
		Assert.noNullElements(variables.keySet(), "variables keys cannot be null");
		return template;
	}

}
