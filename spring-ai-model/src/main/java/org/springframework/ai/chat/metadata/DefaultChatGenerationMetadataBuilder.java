package org.springframework.ai.chat.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.metadata.ChatGenerationMetadata.Builder;

public class DefaultChatGenerationMetadataBuilder implements Builder {

	private String finishReason;

	private Map<String, Object> metadata = new HashMap<>();

	private Set<String> contentFilters = new HashSet<>();

	DefaultChatGenerationMetadataBuilder() {
	}

	@Override
	public Builder finishReason(String finishReason) {
		this.finishReason = finishReason;
		return this;
	}

	@Override
	public <T> Builder metadata(String key, T value) {
		this.metadata.put(key, value);
		return this;
	}

	@Override
	public Builder metadata(Map<String, Object> metadata) {
		this.metadata.putAll(metadata);
		return this;
	}

	@Override
	public Builder contentFilter(String contentFilter) {
		this.contentFilters.add(contentFilter);
		return this;
	}

	@Override
	public Builder contentFilters(Set<String> contentFilters) {
		this.contentFilters.addAll(contentFilters);
		return this;
	}

	@Override
	public ChatGenerationMetadata build() {
		return new DefaultChatGenerationMetadata(this.metadata, this.finishReason, this.contentFilters);
	}

}
