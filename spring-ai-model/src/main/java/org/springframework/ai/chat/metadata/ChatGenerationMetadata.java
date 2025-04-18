package org.springframework.ai.chat.metadata;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.ai.model.ResultMetadata;

public interface ChatGenerationMetadata extends ResultMetadata {

	ChatGenerationMetadata NULL = builder().build();

	String getFinishReason();

	Set<String> getContentFilters();

	<T> T get(String key);

	boolean containsKey(String key);

	<T> T getOrDefault(String key, T defaultObject);

	Set<Entry<String, Object>> entrySet();

	Set<String> keySet();

	boolean isEmpty();

	static Builder builder() {
		return new DefaultChatGenerationMetadataBuilder();
	}

	public interface Builder {

		Builder finishReason(String id);

		<T> Builder metadata(String key, T value);

		Builder metadata(Map<String, Object> metadata);

		Builder contentFilter(String contentFilter);

		Builder contentFilters(Set<String> contentFilters);

		ChatGenerationMetadata build();

	}

}
