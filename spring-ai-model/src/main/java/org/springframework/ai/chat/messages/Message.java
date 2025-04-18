package org.springframework.ai.chat.messages;

import org.springframework.ai.content.Content;
import org.springframework.ai.content.Media;

public interface Message extends Content {

	MessageType getMessageType();

}
