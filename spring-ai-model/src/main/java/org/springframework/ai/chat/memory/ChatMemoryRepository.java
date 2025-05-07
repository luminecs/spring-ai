package org.springframework.ai.chat.memory;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ChatMemoryRepository {

	List<String> findConversationIds();

	List<Message> findByConversationId(String conversationId);

	void saveAll(String conversationId, List<Message> messages);

	void deleteByConversationId(String conversationId);

}
