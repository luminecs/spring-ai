package org.springframework.ai.model.tool;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

public record DefaultToolExecutionResult(List<Message> conversationHistory,
		boolean returnDirect) implements ToolExecutionResult {

	public DefaultToolExecutionResult {
		Assert.notNull(conversationHistory, "conversationHistory cannot be null");
		Assert.noNullElements(conversationHistory, "conversationHistory cannot contain null elements");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private List<Message> conversationHistory = List.of();

		private boolean returnDirect;

		private Builder() {
		}

		public Builder conversationHistory(List<Message> conversationHistory) {
			this.conversationHistory = conversationHistory;
			return this;
		}

		public Builder returnDirect(boolean returnDirect) {
			this.returnDirect = returnDirect;
			return this;
		}

		public DefaultToolExecutionResult build() {
			return new DefaultToolExecutionResult(this.conversationHistory, this.returnDirect);
		}

	}

}
