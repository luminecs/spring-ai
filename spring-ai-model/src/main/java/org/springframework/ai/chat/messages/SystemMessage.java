package org.springframework.ai.chat.messages;

import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.Resource;

public class SystemMessage extends AbstractMessage {

	public SystemMessage(String textContent) {
		super(MessageType.SYSTEM, textContent, Map.of());
	}

	public SystemMessage(Resource resource) {
		super(MessageType.SYSTEM, resource, Map.of());
	}

	@Override
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

}
