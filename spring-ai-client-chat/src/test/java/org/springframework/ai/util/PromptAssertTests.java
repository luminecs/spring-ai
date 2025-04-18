package org.springframework.ai.util;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.PromptTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptAssertTests {

	@Test
	void whenPlaceholderIsPresentThenOk() {
		var promptTemplate = new PromptTemplate("Hello, {name}!");
		PromptAssert.templateHasRequiredPlaceholders(promptTemplate, "{name}");
	}

	@Test
	void whenPlaceholderIsPresentThenThrow() {
		PromptTemplate promptTemplate = new PromptTemplate("Hello, {name}!");
		assertThatThrownBy(() -> PromptAssert.templateHasRequiredPlaceholders(promptTemplate, "{name}", "{age}"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("age");
	}

	@Test
	void whenPromptTemplateIsNullThenThrow() {
		assertThatThrownBy(() -> PromptAssert.templateHasRequiredPlaceholders(null, "{name}"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("promptTemplate cannot be null");
	}

	@Test
	void whenPlaceholdersIsNullThenThrow() {
		assertThatThrownBy(
				() -> PromptAssert.templateHasRequiredPlaceholders(new PromptTemplate("{query}"), (String[]) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("placeholders cannot be null or empty");
	}

	@Test
	void whenPlaceholdersIsEmptyThenThrow() {
		assertThatThrownBy(() -> PromptAssert.templateHasRequiredPlaceholders(new PromptTemplate("{query}")))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("placeholders cannot be null or empty");
	}

}
