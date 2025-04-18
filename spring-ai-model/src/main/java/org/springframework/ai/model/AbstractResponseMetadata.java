package org.springframework.ai.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;

public class AbstractResponseMetadata {

	protected static final String AI_METADATA_STRING = "{ id: %1$s, usage: %2$s, rateLimit: %3$s }";

	protected final Map<String, Object> map = new ConcurrentHashMap<>();

	public AbstractResponseMetadata() {
	}

	@Nullable
	public <T> T get(String key) {
		return (T) this.map.get(key);
	}

	@NonNull
	public <T> T getRequired(Object key) {
		T object = (T) this.map.get(key);
		if (object == null) {
			throw new IllegalArgumentException("Context does not have an entry for key [" + key + "]");
		}
		return object;
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public <T> T getOrDefault(Object key, T defaultObject) {
		return (T) this.map.getOrDefault(key, defaultObject);
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return Collections.unmodifiableMap(this.map).entrySet();
	}

	public Set<String> keySet() {
		return Collections.unmodifiableSet(this.map.keySet());
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

}
