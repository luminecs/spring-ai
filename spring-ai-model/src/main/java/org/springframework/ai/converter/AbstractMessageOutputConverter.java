package org.springframework.ai.converter;

import org.springframework.messaging.converter.MessageConverter;

public abstract class AbstractMessageOutputConverter<T> implements StructuredOutputConverter<T> {

	private MessageConverter messageConverter;

	public AbstractMessageOutputConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public MessageConverter getMessageConverter() {
		return this.messageConverter;
	}

}
