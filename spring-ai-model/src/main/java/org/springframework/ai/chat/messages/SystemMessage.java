package org.springframework.ai.chat.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class SystemMessage extends AbstractMessage {

	public SystemMessage(String textContent) {
		this(textContent, Map.of());
	}

	public SystemMessage(Resource resource) {
		this(MessageUtils.readResource(resource), Map.of());
	}

	private SystemMessage(String textContent, Map<String, Object> metadata) {
		super(MessageType.SYSTEM, textContent, metadata);
	}

	@Override
	@NonNull
	public String getText() {
		return this.textContent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SystemMessage that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return Objects.equals(this.textContent, that.textContent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.textContent);
	}

	@Override
	public String toString() {
		return "SystemMessage{" + "textContent='" + this.textContent + '\'' + ", messageType=" + this.messageType
				+ ", metadata=" + this.metadata + '}';
	}

	public SystemMessage copy() {
		return new SystemMessage(getText(), Map.copyOf(this.metadata));
	}

	public Builder mutate() {
		return new Builder().text(this.textContent).metadata(this.metadata);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		@Nullable
		private String textContent;

		@Nullable
		private Resource resource;

		private Map<String, Object> metadata = new HashMap<>();

		public Builder text(String textContent) {
			this.textContent = textContent;
			return this;
		}

		public Builder text(Resource resource) {
			this.resource = resource;
			return this;
		}

		public Builder metadata(Map<String, Object> metadata) {
			this.metadata = metadata;
			return this;
		}

		public SystemMessage build() {
			if (StringUtils.hasText(textContent) && resource != null) {
				throw new IllegalArgumentException("textContent and resource cannot be set at the same time");
			}
			else if (resource != null) {
				this.textContent = MessageUtils.readResource(resource);
			}
			return new SystemMessage(this.textContent, this.metadata);
		}

	}

}
