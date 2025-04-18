package org.springframework.ai.chat.prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;

public class ChatPromptTemplate implements PromptTemplateActions, PromptTemplateChatActions {

	private final List<PromptTemplate> promptTemplates;

	public ChatPromptTemplate(List<PromptTemplate> promptTemplates) {
		this.promptTemplates = promptTemplates;
	}

	@Override
	public String render() {
		StringBuilder sb = new StringBuilder();
		for (PromptTemplate promptTemplate : this.promptTemplates) {
			sb.append(promptTemplate.render());
		}
		return sb.toString();
	}

	@Override
	public String render(Map<String, Object> model) {
		StringBuilder sb = new StringBuilder();
		for (PromptTemplate promptTemplate : this.promptTemplates) {
			sb.append(promptTemplate.render(model));
		}
		return sb.toString();
	}

	@Override
	public List<Message> createMessages() {
		List<Message> messages = new ArrayList<>();
		for (PromptTemplate promptTemplate : this.promptTemplates) {
			messages.add(promptTemplate.createMessage());
		}
		return messages;
	}

	@Override
	public List<Message> createMessages(Map<String, Object> model) {
		List<Message> messages = new ArrayList<>();
		for (PromptTemplate promptTemplate : this.promptTemplates) {
			messages.add(promptTemplate.createMessage(model));
		}
		return messages;
	}

	@Override
	public Prompt create() {
		List<Message> messages = createMessages();
		return new Prompt(messages);
	}

	@Override
	public Prompt create(ChatOptions modelOptions) {
		List<Message> messages = createMessages();
		return new Prompt(messages, modelOptions);
	}

	@Override
	public Prompt create(Map<String, Object> model) {
		List<Message> messages = createMessages(model);
		return new Prompt(messages);
	}

	@Override
	public Prompt create(Map<String, Object> model, ChatOptions modelOptions) {
		List<Message> messages = createMessages(model);
		return new Prompt(messages, modelOptions);
	}

}
