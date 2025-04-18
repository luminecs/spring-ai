package org.springframework.ai.rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;

public record Query(String text, List<Message> history, Map<String, Object> context) {

	public Query {
		Assert.hasText(text, "text cannot be null or empty");
		Assert.notNull(history, "history cannot be null");
		Assert.noNullElements(history, "history elements cannot be null");
		Assert.notNull(context, "context cannot be null");
		Assert.noNullElements(context.keySet(), "context keys cannot be null");
	}

	public Query(String text) {
		this(text, List.of(), Map.of());
	}

	public Builder mutate() {
		return new Builder().text(this.text).history(this.history).context(this.context);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private String text;

		private List<Message> history = List.of();

		private Map<String, Object> context = Map.of();

		private Builder() {
		}

		public Builder text(String text) {
			this.text = text;
			return this;
		}

		public Builder history(List<Message> history) {
			this.history = history;
			return this;
		}

		public Builder history(Message... history) {
			this.history = List.of(history);
			return this;
		}

		public Builder context(Map<String, Object> context) {
			this.context = context;
			return this;
		}

		public Query build() {
			return new Query(this.text, this.history, this.context);
		}

	}

}
