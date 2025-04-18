package org.springframework.ai.chat.messages;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class AssistantMessage extends AbstractMessage implements MediaContent {

	private final List<ToolCall> toolCalls;

	protected final List<Media> media;

	public AssistantMessage(String content) {
		this(content, Map.of());
	}

	public AssistantMessage(String content, Map<String, Object> properties) {
		this(content, properties, List.of());
	}

	public AssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls) {
		this(content, properties, toolCalls, List.of());
	}

	public AssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls,
			List<Media> media) {
		super(MessageType.ASSISTANT, content, properties);
		Assert.notNull(toolCalls, "Tool calls must not be null");
		Assert.notNull(media, "Media must not be null");
		this.toolCalls = toolCalls;
		this.media = media;
	}

	public List<ToolCall> getToolCalls() {
		return this.toolCalls;
	}

	public boolean hasToolCalls() {
		return !CollectionUtils.isEmpty(this.toolCalls);
	}

	@Override
	public List<Media> getMedia() {
		return this.media;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AssistantMessage that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return Objects.equals(this.toolCalls, that.toolCalls) && Objects.equals(this.media, that.media);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.toolCalls, this.media);
	}

	@Override
	public String toString() {
		return "AssistantMessage [messageType=" + this.messageType + ", toolCalls=" + this.toolCalls + ", textContent="
				+ this.textContent + ", metadata=" + this.metadata + "]";
	}

	public record ToolCall(String id, String type, String name, String arguments) {

	}

}
