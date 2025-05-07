package org.springframework.ai.chat.memory;

import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

import java.util.List;

public interface ChatMemory {

	String DEFAULT_CONVERSATION_ID = "default";

	default void add(String conversationId, Message message) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(message, "message cannot be null");
		this.add(conversationId, List.of(message));
	}

	void add(String conversationId, List<Message> messages);

	default List<Message> get(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return get(conversationId, Integer.MAX_VALUE);
	}

	@Deprecated
	List<Message> get(String conversationId, int lastN);

	void clear(String conversationId);

}
