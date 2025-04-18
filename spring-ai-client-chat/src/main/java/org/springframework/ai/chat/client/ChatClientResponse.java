package org.springframework.ai.chat.client;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public record ChatClientResponse(@Nullable ChatResponse chatResponse, Map<String, Object> context) {

	public ChatClientResponse {
		Assert.notNull(context, "context cannot be null");
		Assert.noNullElements(context.keySet(), "context keys cannot be null");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ChatResponse chatResponse;

		private Map<String, Object> context = new HashMap<>();

		private Builder() {
		}

		public Builder chatResponse(ChatResponse chatResponse) {
			this.chatResponse = chatResponse;
			return this;
		}

		public Builder context(Map<String, Object> context) {
			Assert.notNull(context, "context cannot be null");
			this.context.putAll(context);
			return this;
		}

		public Builder context(String key, Object value) {
			Assert.notNull(key, "key cannot be null");
			this.context.put(key, value);
			return this;
		}

		public ChatClientResponse build() {
			return new ChatClientResponse(this.chatResponse, this.context);
		}

	}

}
