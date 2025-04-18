package org.springframework.ai.watsonx.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

// @formatter:off
public final class MessageToPromptConverter {

	public static final String TOOL_EXECUTION_NOT_SUPPORTED_FOR_WAI_MODELS = "Tool execution results are not supported for watsonx.ai models";

	private static final String HUMAN_PROMPT = "Human: ";

	private static final String ASSISTANT_PROMPT = "Assistant: ";

	private String humanPrompt = HUMAN_PROMPT;
	private String assistantPrompt = ASSISTANT_PROMPT;

	private MessageToPromptConverter() {
	}

	public static MessageToPromptConverter create() {
		return new MessageToPromptConverter();
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
				.collect(Collectors.joining("\n"));

		final String userMessages = messages.stream()
				.filter(message -> message.getMessageType() == MessageType.USER
						|| message.getMessageType() == MessageType.ASSISTANT)
				.map(this::messageToString)
				.collect(Collectors.joining("\n"));

		return String.format("%s%n%n%s%n%s", systemMessages, userMessages, this.assistantPrompt).trim();
	}

	protected String messageToString(Message message) {
		switch (message.getMessageType()) {
			case SYSTEM:
				return message.getText();
			case USER:
				return this.humanPrompt + message.getText();
			case ASSISTANT:
				return this.assistantPrompt + message.getText();
			case TOOL:
				throw new IllegalArgumentException(TOOL_EXECUTION_NOT_SUPPORTED_FOR_WAI_MODELS);
		}
		throw new IllegalArgumentException("Unknown message type: " + message.getMessageType());
	}
	// @formatter:on

}
