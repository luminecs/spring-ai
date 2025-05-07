package org.springframework.ai.chat.prompt;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptTemplateBuilderTests {

	@Test
	void builderNullTemplateShouldThrow() {
		assertThatThrownBy(() -> PromptTemplate.builder().template(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("template cannot be null or empty");
	}

	@Test
	void builderEmptyTemplateShouldThrow() {
		assertThatThrownBy(() -> PromptTemplate.builder().template("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("template cannot be null or empty");
	}

	@Test
	void builderNullResourceShouldThrow() {
		assertThatThrownBy(() -> PromptTemplate.builder().resource(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("resource cannot be null");
	}

	@Test
	void builderNullVariablesShouldThrow() {
		assertThatThrownBy(() -> PromptTemplate.builder().variables(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("variables cannot be null");
	}

	@Test
	void builderNullVariableKeyShouldThrow() {
		Map<String, Object> variables = new HashMap<>();
		variables.put(null, "value");
		assertThatThrownBy(() -> PromptTemplate.builder().variables(variables))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("variables keys cannot be null");
	}

	@Test
	void builderNullRendererShouldThrow() {
		assertThatThrownBy(() -> PromptTemplate.builder().renderer(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("renderer cannot be null");
	}

	@Test
	void renderWithMissingVariableShouldThrow() {

		PromptTemplate promptTemplate = PromptTemplate.builder()
			.template("Hello {name}!")

			.build();

		try {
			promptTemplate.render();

			Assertions.fail("Expected IllegalStateException was not thrown.");
		}
		catch (IllegalStateException e) {

			assertThat(e.getMessage())
				.isEqualTo("Not all variables were replaced in the template. Missing variable names are: [name].");
		}
		catch (Exception e) {

			Assertions.fail("Caught unexpected exception: " + e.getClass().getName());
		}
	}

}
