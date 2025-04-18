package org.springframework.ai.bedrock;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

public final class MessageToPromptConverter {

	private static final String HUMAN_PROMPT = "Human:";

	private static final String ASSISTANT_PROMPT = "Assistant:";

	private final String lineSeparator;

	private String humanPrompt = HUMAN_PROMPT;

	private String assistantPrompt = ASSISTANT_PROMPT;

	private MessageToPromptConverter(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public static MessageToPromptConverter create() {
		return create(System.lineSeparator());
	}

	public static MessageToPromptConverter create(String lineSeparator) {
		return new MessageToPromptConverter(lineSeparator);
	}

	public MessageToPromptConverter withHumanPrompt(String humanPrompt) {
		this.humanPrompt = humanPrompt;
		return this;
	}

	public MessageToPromptConverter withAssistantPrompt(String assistantPrompt) {
		this.assistantPrompt = assistantPrompt;
		return this;
	}

	public String toPrompt(List<Message> messages) {

		final String systemMessages = messages.stream()
			.filter(message -> message.getMessageType() == MessageType.SYSTEM)
			.map(Message::getText)
			.collect(Collectors.joining(System.lineSeparator()));

		final String userMessages = messages.stream()
			.filter(message -> message.getMessageType() == MessageType.USER
					|| message.getMessageType() == MessageType.ASSISTANT)
			.map(this::messageToString)
			.collect(Collectors.joining(System.lineSeparator()));

		return systemMessages + this.lineSeparator + this.lineSeparator + userMessages + this.lineSeparator
				+ ASSISTANT_PROMPT;
	}

	protected String messageToString(Message message) {
		switch (message.getMessageType()) {
			case SYSTEM:
				return message.getText();
			case USER:
				return this.humanPrompt + " " + message.getText();
			case ASSISTANT:
				return this.assistantPrompt + " " + message.getText();
			case TOOL:
				throw new IllegalArgumentException("Tool execution results are not supported for Bedrock models");
		}

		throw new IllegalArgumentException("Unknown message type: " + message.getMessageType());
	}

}
