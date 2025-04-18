package org.springframework.ai.vectorstore.qdrant;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.qdrant.client.ValueFactory;
import io.qdrant.client.grpc.JsonWithInt.Struct;
import io.qdrant.client.grpc.JsonWithInt.Value;

import org.springframework.util.Assert;

final class QdrantValueFactory {

	private QdrantValueFactory() {
	}

	public static Map<String, Value> toValueMap(Map<String, Object> inputMap) {
		Assert.notNull(inputMap, "Input map must not be null");

		return inputMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> value(e.getValue())));
	}

	@SuppressWarnings("unchecked")
	private static Value value(Object value) {

		if (value == null) {
			return ValueFactory.nullValue();
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			Object[] objectArray = new Object[length];
			for (int i = 0; i < length; i++) {
				objectArray[i] = Array.get(value, i);
			}
			return value(objectArray);
		}

		if (value instanceof Map) {
			return value((Map<String, Object>) value);
		}

		switch (value.getClass().getSimpleName()) {
			case "String":
				return ValueFactory.value((String) value);
			case "Integer":
				return ValueFactory.value((Integer) value);
			case "Double":
				return ValueFactory.value((Double) value);
			case "Float":
				return ValueFactory.value((Float) value);
			case "Boolean":
				return ValueFactory.value((Boolean) value);
			default:
				throw new IllegalArgumentException("Unsupported Qdrant value type: " + value.getClass());
		}
	}

	private static Value value(Object[] elements) {
		List<Value> values = new ArrayList<Value>(elements.length);

		for (Object element : elements) {
			values.add(value(element));
		}

		return ValueFactory.list(values);
	}

	private static Value value(Map<String, Object> inputMap) {
		Struct.Builder structBuilder = Struct.newBuilder();
		Map<String, Value> map = toValueMap(inputMap);
		structBuilder.putAllFields(map);
		return Value.newBuilder().setStructValue(structBuilder).build();
	}

}
