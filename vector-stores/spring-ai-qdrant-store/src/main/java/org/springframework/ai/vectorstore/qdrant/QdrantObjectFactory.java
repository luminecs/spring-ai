package org.springframework.ai.vectorstore.qdrant;

import java.util.Map;
import java.util.stream.Collectors;

import io.qdrant.client.grpc.JsonWithInt.ListValue;
import io.qdrant.client.grpc.JsonWithInt.Value;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;

final class QdrantObjectFactory {

	private static final Log logger = LogFactory.getLog(QdrantObjectFactory.class);

	private QdrantObjectFactory() {
	}

	public static Map<String, Object> toObjectMap(Map<String, Value> payload) {
		Assert.notNull(payload, "Payload map must not be null");
		return payload.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> object(e.getValue())));
	}

	private static Object object(ListValue listValue) {
		return listValue.getValuesList().stream().map(QdrantObjectFactory::object).collect(Collectors.toList());
	}

	private static Object object(Value value) {

		switch (value.getKindCase()) {
			case INTEGER_VALUE:
				return value.getIntegerValue();
			case STRING_VALUE:
				return value.getStringValue();
			case DOUBLE_VALUE:
				return value.getDoubleValue();
			case BOOL_VALUE:
				return value.getBoolValue();
			case LIST_VALUE:
				return object(value.getListValue());
			case STRUCT_VALUE:
				return toObjectMap(value.getStructValue().getFieldsMap());
			case NULL_VALUE:
				return null;
			case KIND_NOT_SET:
			default:
				logger.warn("Unsupported value type: " + value.getKindCase());
				return null;
		}

	}

}
