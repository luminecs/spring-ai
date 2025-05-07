package org.springframework.ai.prompt;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class PromptTests {

	@Test
	void newApiPlaygroundTests() {

		String templateText = "Hello '{firstName}' '{lastName}' from Unix";
		PromptTemplate pt = new PromptTemplate(templateText);

		final Map<String, Object> model = new HashMap<>();
		model.put("firstName", "Nick");

		Assertions.assertThatThrownBy(() -> pt.render(model))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Not all variables were replaced in the template. Missing variable names are: [lastName].");

		pt.add("lastName", "Park");
		String promptString = pt.render(model);
		assertThat(promptString).isEqualTo("Hello 'Nick' 'Park' from Unix");

		promptString = pt.render(model);
		assertThat(promptString).isEqualTo("Hello 'Nick' 'Park' from Unix");

		Prompt prompt = pt.create(model);
		assertThat(prompt.getContents()).isNotNull();
		assertThat(prompt.getInstructions()).isNotEmpty().hasSize(1);
		System.out.println(prompt.getContents());

		String systemTemplate = "You are a helpful assistant that translates {input_language} to {output_language}.";

		Map<String, Object> systemModel = new HashMap();
		systemModel.put("input_language", "English");
		systemModel.put("output_language", "French");

		String humanTemplate = "{text}";
		Map<String, Object> humanModel = new HashMap();
		humanModel.put("text", "I love programming");

		PromptTemplate promptTemplate = new SystemPromptTemplate(systemTemplate);
		Prompt systemPrompt = promptTemplate.create(systemModel);

		promptTemplate = new PromptTemplate(humanTemplate);

		Prompt humanPrompt = promptTemplate.create(humanModel);

	}

	@Test
	public void testPromptCopy() {
		String template = "Hello, {name}! Your age is {age}.";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Alice");
		model.put("age", 30);
		PromptTemplate promptTemplate = PromptTemplate.builder().template(template).variables(model).build();
		ChatOptions chatOptions = ChatOptions.builder().temperature(0.5).maxTokens(100).build();

		Prompt prompt = promptTemplate.create(model, chatOptions);

		Prompt copiedPrompt = prompt.copy();
		assertThat(prompt).isNotSameAs(copiedPrompt);
		assertThat(prompt.getOptions()).isNotSameAs(copiedPrompt.getOptions());
		assertThat(prompt.getInstructions()).isNotSameAs(copiedPrompt.getInstructions());
	}

	@Test
	public void mutatePrompt() {
		String template = "Hello, {name}! Your age is {age}.";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Alice");
		model.put("age", 30);
		PromptTemplate promptTemplate = PromptTemplate.builder().template(template).variables(model).build();
		ChatOptions chatOptions = ChatOptions.builder().temperature(0.5).maxTokens(100).build();

		Prompt prompt = promptTemplate.create(model, chatOptions);

		Prompt copiedPrompt = prompt.mutate().build();
		assertThat(prompt).isNotSameAs(copiedPrompt);
		assertThat(prompt.getOptions()).isNotSameAs(copiedPrompt.getOptions());
		assertThat(prompt.getInstructions()).isNotSameAs(copiedPrompt.getInstructions());
	}

}
