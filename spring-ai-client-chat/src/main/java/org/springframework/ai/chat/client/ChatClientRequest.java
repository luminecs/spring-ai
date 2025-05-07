package org.springframework.ai.chat.client;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public record ChatClientRequest(Prompt prompt, Map<String, Object> context) {

	public ChatClientRequest {
		Assert.notNull(prompt, "prompt cannot be null");
		Assert.notNull(context, "context cannot be null");
		Assert.noNullElements(context.keySet(), "context keys cannot be null");
	}

	public ChatClientRequest copy() {
		return new ChatClientRequest(this.prompt.copy(), new HashMap<>(this.context));
	}

	public Builder mutate() {
		return new Builder().prompt(this.prompt.copy()).context(new HashMap<>(this.context));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Prompt prompt;

		private Map<String, Object> context = new HashMap<>();

		private Builder() {
		}

		public Builder prompt(Prompt prompt) {
			Assert.notNull(prompt, "prompt cannot be null");
			this.prompt = prompt;
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

		public ChatClientRequest build() {
			return new ChatClientRequest(prompt, context);
		}

	}

}
