package org.springframework.ai.chat.prompt;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.content.Media;

public interface PromptTemplateMessageActions {

	Message createMessage();

	Message createMessage(List<Media> mediaList);

	Message createMessage(Map<String, Object> model);

}
