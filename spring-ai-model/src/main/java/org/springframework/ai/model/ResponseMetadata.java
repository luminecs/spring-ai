package org.springframework.ai.model;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;

public interface ResponseMetadata {

	@Nullable
	<T> T get(String key);

	@NonNull
	<T> T getRequired(Object key);

	boolean containsKey(Object key);

	<T> T getOrDefault(Object key, T defaultObject);

	default <T> T getOrDefault(String key, Supplier<T> defaultObjectSupplier) {
		T value = get(key);
		return value != null ? value : defaultObjectSupplier.get();
	}

	Set<Map.Entry<String, Object>> entrySet();

	Set<String> keySet();

	boolean isEmpty();

}
