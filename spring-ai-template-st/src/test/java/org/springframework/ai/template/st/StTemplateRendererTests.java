package org.springframework.ai.template.st;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ai.template.ValidationMode;

class StTemplateRendererTests {

	@Test
	void shouldNotAcceptNullValidationMode() {
		assertThatThrownBy(() -> StTemplateRenderer.builder().validationMode(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("validationMode cannot be null");
	}

	@Test
	void shouldUseDefaultValuesWhenUsingBuilder() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();

		assertThat(ReflectionTestUtils.getField(renderer, "startDelimiterToken")).isEqualTo('{');
		assertThat(ReflectionTestUtils.getField(renderer, "endDelimiterToken")).isEqualTo('}');
		assertThat(ReflectionTestUtils.getField(renderer, "validationMode")).isEqualTo(ValidationMode.THROW);
	}

	@Test
	void shouldRenderTemplateWithSingleVariable() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "Spring AI");

		String result = renderer.apply("Hello {name}!", variables);

		assertThat(result).isEqualTo("Hello Spring AI!");
	}

	@Test
	void shouldRenderTemplateWithMultipleVariables() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("greeting", "Hello");
		variables.put("name", "Spring AI");
		variables.put("punctuation", "!");

		String result = renderer.apply("{greeting} {name}{punctuation}", variables);

		assertThat(result).isEqualTo("Hello Spring AI!");
	}

	@Test
	void shouldNotRenderEmptyTemplate() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();

		assertThatThrownBy(() -> renderer.apply("", variables)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("template cannot be null or empty");
	}

	@Test
	void shouldNotAcceptNullVariables() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		assertThatThrownBy(() -> renderer.apply("Hello!", null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("variables cannot be null");
	}

	@Test
	void shouldNotAcceptVariablesWithNullKeySet() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		String template = "Hello!";
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put(null, "Spring AI");

		assertThatThrownBy(() -> renderer.apply(template, variables)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("variables keys cannot be null");
	}

	@Test
	void shouldThrowExceptionForInvalidTemplateSyntax() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "Spring AI");

		assertThatThrownBy(() -> renderer.apply("Hello {name!", variables)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("The template string is not valid.");
	}

	@Test
	void shouldThrowExceptionForMissingVariablesInThrowMode() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("greeting", "Hello");

		assertThatThrownBy(() -> renderer.apply("{greeting} {name}!", variables))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining(
					"Not all variables were replaced in the template. Missing variable names are: [name]");
	}

	@Test
	void shouldContinueRenderingWithMissingVariablesInWarnMode() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().validationMode(ValidationMode.WARN).build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("greeting", "Hello");

		String result = renderer.apply("{greeting} {name}!", variables);

		assertThat(result).isEqualTo("Hello !");
	}

	@Test
	void shouldRenderWithoutValidationInNoneMode() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().validationMode(ValidationMode.NONE).build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("greeting", "Hello");

		String result = renderer.apply("{greeting} {name}!", variables);

		assertThat(result).isEqualTo("Hello !");
	}

	@Test
	void shouldRenderWithCustomDelimiters() {
		StTemplateRenderer renderer = StTemplateRenderer.builder()
			.startDelimiterToken('<')
			.endDelimiterToken('>')
			.build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "Spring AI");

		String result = renderer.apply("Hello <name>!", variables);

		assertThat(result).isEqualTo("Hello Spring AI!");
	}

	@Test
	void shouldHandleSpecialCharactersAsDelimiters() {
		StTemplateRenderer renderer = StTemplateRenderer.builder()
			.startDelimiterToken('$')
			.endDelimiterToken('$')
			.build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("name", "Spring AI");

		String result = renderer.apply("Hello $name$!", variables);

		assertThat(result).isEqualTo("Hello Spring AI!");
	}

	@Test
	void shouldHandleComplexTemplateStructures() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("header", "Welcome");
		variables.put("user", "Spring AI");
		variables.put("items", "one, two, three");
		variables.put("footer", "Goodbye");

		String result = renderer.apply("""
				{header}
				User: {user}
				Items: {items}
				{footer}
				""", variables);

		assertThat(result).isEqualToNormalizingNewlines("""
				Welcome
				User: Spring AI
				Items: one, two, three
				Goodbye
				""");
	}

	@Test
	void shouldHandleListVariables() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().validationMode(ValidationMode.NONE).build();

		Map<String, Object> variables = new HashMap<>();
		variables.put("items", new String[] { "apple", "banana", "cherry" });

		String result = renderer.apply("Items: {items; separator=\", \"}", variables);

		assertThat(result).isEqualTo("Items: apple, banana, cherry");
	}

	@Test
	void shouldRenderTemplateWithOptions() {

		StTemplateRenderer renderer = StTemplateRenderer.builder().validationMode(ValidationMode.NONE).build();

		Map<String, Object> variables = new HashMap<>();
		variables.put("fruits", new String[] { "apple", "banana", "cherry" });
		variables.put("count", 3);

		String result = renderer.apply("Fruits: {fruits; separator=\", \"}, Count: {count}", variables);

		assertThat(result).isEqualTo("Fruits: apple, banana, cherry, Count: 3");

		assertThat(result).contains("apple");
		assertThat(result).contains("banana");
		assertThat(result).contains("cherry");
	}

	@Test
	void shouldHandleNumericVariables() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("integer", 42);
		variables.put("float", 3.14);

		String result = renderer.apply("Integer: {integer}, Float: {float}", variables);

		assertThat(result).isEqualTo("Integer: 42, Float: 3.14");
	}

	@Test
	void shouldHandleObjectVariables() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().build();
		Map<String, Object> variables = new HashMap<>();

		variables.put("name", "John");
		variables.put("age", 30);

		String result = renderer.apply("Person: {name}, Age: {age}", variables);

		assertThat(result).isEqualTo("Person: John, Age: 30");
	}

	@Test
	void shouldRenderTemplateWithSupportStFunctions() {
		StTemplateRenderer renderer = StTemplateRenderer.builder().supportStFunctions().build();
		Map<String, Object> variables = new HashMap<>();
		variables.put("memory", "you are a helpful assistant");
		String template = "{if(strlen(memory))}Hello!{endif}";

		String result = renderer.apply(template, variables);

		assertThat(result).isEqualTo("Hello!");
	}

}
