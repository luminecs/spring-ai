package org.springframework.ai.chat.prompt;

import java.util.Map;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.core.io.Resource;

public class AssistantPromptTemplate extends PromptTemplate {

	public AssistantPromptTemplate(String template) {
		super(template);
	}

	public AssistantPromptTemplate(Resource resource) {
		super(resource);
	}

	@Override
	public Prompt create() {
		return new Prompt(new AssistantMessage(render()));
	}

	@Override
	public Prompt create(Map<String, Object> model) {
		return new Prompt(new AssistantMessage(render(model)));
	}

	@Override
	public Message createMessage() {
		return new AssistantMessage(render());
	}

	@Override
	public Message createMessage(Map<String, Object> model) {
		return new AssistantMessage(render(model));
	}

}
