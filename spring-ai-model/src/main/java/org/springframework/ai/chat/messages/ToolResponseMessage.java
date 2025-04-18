package org.springframework.ai.chat.messages;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ToolResponseMessage extends AbstractMessage {

	protected final List<ToolResponse> responses;

	public ToolResponseMessage(List<ToolResponse> responses) {
		this(responses, Map.of());
	}

	public ToolResponseMessage(List<ToolResponse> responses, Map<String, Object> metadata) {
		super(MessageType.TOOL, "", metadata);
		this.responses = responses;
	}

	public List<ToolResponse> getResponses() {
		return this.responses;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ToolResponseMessage that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return Objects.equals(this.responses, that.responses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.responses);
	}

	@Override
	public String toString() {
		return "ToolResponseMessage{" + "responses=" + this.responses + ", messageType=" + this.messageType
				+ ", metadata=" + this.metadata + '}';
	}

	public record ToolResponse(String id, String name, String responseData) {

	}

}
