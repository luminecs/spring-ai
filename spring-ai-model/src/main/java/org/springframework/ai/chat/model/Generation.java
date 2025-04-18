package org.springframework.ai.chat.model;

import java.util.Objects;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.model.ModelResult;

public class Generation implements ModelResult<AssistantMessage> {

	private final AssistantMessage assistantMessage;

	private ChatGenerationMetadata chatGenerationMetadata;

	public Generation(AssistantMessage assistantMessage) {
		this(assistantMessage, ChatGenerationMetadata.NULL);
	}

	public Generation(AssistantMessage assistantMessage, ChatGenerationMetadata chatGenerationMetadata) {
		this.assistantMessage = assistantMessage;
		this.chatGenerationMetadata = chatGenerationMetadata;
	}

	@Override
	public AssistantMessage getOutput() {
		return this.assistantMessage;
	}

	@Override
	public ChatGenerationMetadata getMetadata() {
		ChatGenerationMetadata chatGenerationMetadata = this.chatGenerationMetadata;
		return chatGenerationMetadata != null ? chatGenerationMetadata : ChatGenerationMetadata.NULL;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Generation that)) {
			return false;
		}
		return Objects.equals(this.assistantMessage, that.assistantMessage)
				&& Objects.equals(this.chatGenerationMetadata, that.chatGenerationMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.assistantMessage, this.chatGenerationMetadata);
	}

	@Override
	public String toString() {
		return "Generation[" + "assistantMessage=" + this.assistantMessage + ", chatGenerationMetadata="
				+ this.chatGenerationMetadata + ']';
	}

}
