package org.springframework.ai.chat.prompt;

public class FunctionPromptTemplate extends PromptTemplate {

	private String name;

	public FunctionPromptTemplate(String template) {
		super(template);
	}

}
