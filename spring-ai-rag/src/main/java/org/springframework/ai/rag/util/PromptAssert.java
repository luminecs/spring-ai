package org.springframework.ai.rag.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.Assert;

public final class PromptAssert {

	private PromptAssert() {
	}

	public static void templateHasRequiredPlaceholders(PromptTemplate promptTemplate, String... placeholders) {
		Assert.notNull(promptTemplate, "promptTemplate cannot be null");
		Assert.notEmpty(placeholders, "placeholders cannot be null or empty");

		List<String> missingPlaceholders = new ArrayList<>();
		for (String placeholder : placeholders) {
			if (!promptTemplate.getTemplate().contains(placeholder)) {
				missingPlaceholders.add(placeholder);
			}
		}

		if (!missingPlaceholders.isEmpty()) {
			throw new IllegalArgumentException("The following placeholders must be present in the prompt template: %s"
				.formatted(String.join(",", missingPlaceholders)));
		}
	}

}
