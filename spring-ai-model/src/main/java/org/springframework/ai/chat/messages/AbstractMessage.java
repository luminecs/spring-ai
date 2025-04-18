package org.springframework.ai.chat.messages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

public abstract class AbstractMessage implements Message {

	public static final String MESSAGE_TYPE = "messageType";

	protected final MessageType messageType;

	@Nullable
	protected final String textContent;

	protected final Map<String, Object> metadata;

	protected AbstractMessage(MessageType messageType, @Nullable String textContent, Map<String, Object> metadata) {
		Assert.notNull(messageType, "Message type must not be null");
		if (messageType == MessageType.SYSTEM || messageType == MessageType.USER) {
			Assert.notNull(textContent, "Content must not be null for SYSTEM or USER messages");
		}
		Assert.notNull(metadata, "Metadata must not be null");
		this.messageType = messageType;
		this.textContent = textContent;
		this.metadata = new HashMap<>(metadata);
		this.metadata.put(MESSAGE_TYPE, messageType);
	}

	protected AbstractMessage(MessageType messageType, Resource resource, Map<String, Object> metadata) {
		Assert.notNull(messageType, "Message type must not be null");
		Assert.notNull(resource, "Resource must not be null");
		Assert.notNull(metadata, "Metadata must not be null");
		try (InputStream inputStream = resource.getInputStream()) {
			this.textContent = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to read resource", ex);
		}
		this.messageType = messageType;
		this.metadata = new HashMap<>(metadata);
		this.metadata.put(MESSAGE_TYPE, messageType);
	}

	@Override
	@Nullable
	public String getText() {
		return this.textContent;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	@Override
	public MessageType getMessageType() {
		return this.messageType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AbstractMessage that)) {
			return false;
		}
		return this.messageType == that.messageType && Objects.equals(this.textContent, that.textContent)
				&& Objects.equals(this.metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messageType, this.textContent, this.metadata);
	}

}
