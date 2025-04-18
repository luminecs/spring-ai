package org.springframework.ai.chat.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class UserMessage extends AbstractMessage implements MediaContent {

	protected final List<Media> media;

	public UserMessage(String textContent) {
		this(MessageType.USER, textContent, new ArrayList<>(), Map.of());
	}

	public UserMessage(Resource resource) {
		super(MessageType.USER, resource, Map.of());
		this.media = new ArrayList<>();
	}

	public UserMessage(String textContent, List<Media> media) {
		this(MessageType.USER, textContent, media, Map.of());
	}

	public UserMessage(String textContent, Media... media) {
		this(textContent, Arrays.asList(media));
	}

	public UserMessage(String textContent, Collection<Media> mediaList, Map<String, Object> metadata) {
		this(MessageType.USER, textContent, mediaList, metadata);
	}

	public UserMessage(MessageType messageType, String textContent, Collection<Media> media,
			Map<String, Object> metadata) {
		super(messageType, textContent, metadata);
		Assert.notNull(media, "media data must not be null");
		this.media = new ArrayList<>(media);
	}

	@Override
	public String toString() {
		return "UserMessage{" + "content='" + getText() + '\'' + ", properties=" + this.metadata + ", messageType="
				+ this.messageType + '}';
	}

	@Override
	public List<Media> getMedia() {
		return this.media;
	}

	@Override
	public String getText() {
		return this.textContent;
	}

}
