package org.springframework.ai.chat.metadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.Assert;

public class DefaultChatGenerationMetadata implements ChatGenerationMetadata {

	private final Map<String, Object> metadata;

	private final String finishReason;

	private final Set<String> contentFilters;

	DefaultChatGenerationMetadata(Map<String, Object> metadata, String finishReason, Set<String> contentFilters) {
		Assert.notNull(metadata, "Metadata must not be null");
		Assert.notNull(contentFilters, "Content filters must not be null");
		this.metadata = metadata;
		this.finishReason = finishReason;
		this.contentFilters = new HashSet<>(contentFilters);
	}

	@Override
	public <T> T get(String key) {
		return (T) this.metadata.get(key);
	}

	@Override
	public boolean containsKey(String key) {
		return this.metadata.containsKey(key);
	}

	@Override
	public <T> T getOrDefault(String key, T defaultObject) {
		return containsKey(key) ? get(key) : defaultObject;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return Collections.unmodifiableSet(this.metadata.entrySet());
	}

	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(this.metadata.keySet());
	}

	@Override
	public boolean isEmpty() {
		return this.metadata.isEmpty();
	}

	@Override
	public String getFinishReason() {
		return this.finishReason;
	}

	@Override
	public Set<String> getContentFilters() {
		return Collections.unmodifiableSet(this.contentFilters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.metadata, this.finishReason, this.contentFilters);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DefaultChatGenerationMetadata other = (DefaultChatGenerationMetadata) obj;
		return Objects.equals(this.metadata, other.metadata) && Objects.equals(this.finishReason, other.finishReason)
				&& Objects.equals(this.contentFilters, other.contentFilters);
	}

	@Override
	public String toString() {
		return String.format("DefaultChatGenerationMetadata[finishReason='%s', filters=%d, metadata=%d]",
				this.finishReason, this.contentFilters.size(), this.metadata.size());
	}

}
