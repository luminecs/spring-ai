package org.springframework.ai.chat.prompt;

import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.core.io.Resource;

public class SystemPromptTemplate extends PromptTemplate {

	public SystemPromptTemplate(String template) {
		super(template);
	}

	public SystemPromptTemplate(Resource resource) {
		super(resource);
	}

	@Override
	public Message createMessage() {
		return new SystemMessage(render());
	}

	@Override
	public Message createMessage(Map<String, Object> model) {
		return new SystemMessage(render(model));
	}

	@Override
	public Prompt create() {
		return new Prompt(new SystemMessage(render()));
	}

	@Override
	public Prompt create(Map<String, Object> model) {
		return new Prompt(new SystemMessage(render(model)));
	}

}
