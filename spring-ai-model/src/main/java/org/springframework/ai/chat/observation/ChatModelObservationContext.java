package org.springframework.ai.chat.observation;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.observation.ModelObservationContext;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.util.Assert;

public class ChatModelObservationContext extends ModelObservationContext<Prompt, ChatResponse> {

	ChatModelObservationContext(Prompt prompt, String provider) {
		super(prompt,
				AiOperationMetadata.builder().operationType(AiOperationType.CHAT.value()).provider(provider).build());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Prompt prompt;

		private String provider;

		private Builder() {
		}

		public Builder prompt(Prompt prompt) {
			this.prompt = prompt;
			return this;
		}

		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public ChatModelObservationContext build() {
			return new ChatModelObservationContext(this.prompt, this.provider);
		}

	}

}
