package org.springframework.ai.chat.prompt;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;

public interface PromptTemplateChatActions {

	List<Message> createMessages();

	List<Message> createMessages(Map<String, Object> model);

}
