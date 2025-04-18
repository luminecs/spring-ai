package org.springframework.ai.chat.memory;

import java.util.List;

import org.springframework.ai.chat.messages.Message;

public interface ChatMemory {

	default void add(String conversationId, Message message) {
		this.add(conversationId, List.of(message));
	}

	void add(String conversationId, List<Message> messages);

	List<Message> get(String conversationId, int lastN);

	void clear(String conversationId);

}
