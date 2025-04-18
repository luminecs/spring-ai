package org.springframework.ai.chat.metadata;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.model.AbstractResponseMetadata;
import org.springframework.ai.model.ResponseMetadata;

public class ChatResponseMetadata extends AbstractResponseMetadata implements ResponseMetadata {

	private static final Logger logger = LoggerFactory.getLogger(ChatResponseMetadata.class);

	private String id = "";

	private String model = "";

	private RateLimit rateLimit = new EmptyRateLimit();

	private Usage usage = new EmptyUsage();

	private PromptMetadata promptMetadata = PromptMetadata.empty();

	public static Builder builder() {
		return new Builder();
	}

	public String getId() {
		return this.id;
	}

	public String getModel() {
		return this.model;
	}

	public RateLimit getRateLimit() {
		return this.rateLimit;
	}

	public Usage getUsage() {
		return this.usage;
	}

	public PromptMetadata getPromptMetadata() {
		return this.promptMetadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ChatResponseMetadata that)) {
			return false;
		}
		return Objects.equals(this.id, that.id) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.rateLimit, that.rateLimit) && Objects.equals(this.usage, that.usage)
				&& Objects.equals(this.promptMetadata, that.promptMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.model, this.rateLimit, this.usage, this.promptMetadata);
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getId(), getUsage(), getRateLimit());
	}

	public static class Builder {

		private final ChatResponseMetadata chatResponseMetadata;

		public Builder() {
			this.chatResponseMetadata = new ChatResponseMetadata();
		}

		public Builder metadata(Map<String, Object> mapToCopy) {
			this.chatResponseMetadata.map.putAll(mapToCopy);
			return this;
		}

		public Builder keyValue(String key, Object value) {
			if (key == null) {
				throw new IllegalArgumentException("Key must not be null");
			}
			if (value != null) {
				this.chatResponseMetadata.map.put(key, value);
			}
			else {
				logger.debug("Ignore null value for key [{}]", key);
			}
			return this;
		}

		public Builder id(String id) {
			this.chatResponseMetadata.id = id;
			return this;
		}

		public Builder model(String model) {
			this.chatResponseMetadata.model = model;
			return this;
		}

		public Builder rateLimit(RateLimit rateLimit) {
			this.chatResponseMetadata.rateLimit = rateLimit;
			return this;
		}

		public Builder usage(Usage usage) {
			this.chatResponseMetadata.usage = usage;
			return this;
		}

		public Builder promptMetadata(PromptMetadata promptMetadata) {
			this.chatResponseMetadata.promptMetadata = promptMetadata;
			return this;
		}

		public ChatResponseMetadata build() {
			return this.chatResponseMetadata;
		}

	}

}
