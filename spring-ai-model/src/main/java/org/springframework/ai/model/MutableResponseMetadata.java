package org.springframework.ai.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;

public class MutableResponseMetadata implements ResponseMetadata {

	private final Map<String, Object> map = new ConcurrentHashMap<>();

	public <T> MutableResponseMetadata put(String key, T object) {
		this.map.put(key, object);
		return this;
	}

	@Override
	@Nullable
	public <T> T get(String key) {
		return (T) this.map.get(key);
	}

	public Object remove(Object key) {
		return this.map.remove(key);
	}

	@Override
	@NonNull
	public <T> T getRequired(Object key) {
		T object = (T) this.map.get(key);
		if (object == null) {
			throw new IllegalArgumentException("Context does not have an entry for key [" + key + "]");
		}
		return object;
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public <T> T getOrDefault(Object key, T defaultObject) {
		return (T) this.map.getOrDefault(key, defaultObject);
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return Collections.unmodifiableMap(this.map).entrySet();
	}

	public Set<String> keySet() {
		return Collections.unmodifiableSet(this.map.keySet());
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public <T> T computeIfAbsent(String key, Function<Object, ? extends T> mappingFunction) {
		return (T) this.map.computeIfAbsent(key, mappingFunction);
	}

	public void clear() {
		this.map.clear();
	}

	public Map<String, Object> getRawMap() {
		return this.map;
	}

}
