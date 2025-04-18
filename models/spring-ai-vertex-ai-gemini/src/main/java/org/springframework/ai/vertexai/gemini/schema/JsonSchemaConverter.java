package org.springframework.ai.vertexai.gemini.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.ai.util.json.JsonParser;
import org.springframework.util.Assert;

public final class JsonSchemaConverter {

	private JsonSchemaConverter() {

	}

	public static ObjectNode fromJson(String jsonString) {
		try {
			return (ObjectNode) JsonParser.getObjectMapper().readTree(jsonString);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse JSON: " + jsonString, e);
		}
	}

	public static ObjectNode convertToOpenApiSchema(ObjectNode jsonSchemaNode) {
		Assert.notNull(jsonSchemaNode, "JSON Schema node must not be null");

		try {

			ObjectNode openApiSchema = convertSchema(jsonSchemaNode, JsonParser.getObjectMapper().getNodeFactory());

			if (!openApiSchema.has("openapi")) {
				openApiSchema.put("openapi", "3.0.0");
			}

			return openApiSchema;
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to convert JSON Schema to OpenAPI format: " + e.getMessage(), e);
		}
	}

	private static void copyCommonProperties(ObjectNode source, ObjectNode target) {
		Assert.notNull(source, "Source node must not be null");
		Assert.notNull(target, "Target node must not be null");
		String[] commonProperties = {

				"type", "format", "description", "default", "maximum", "minimum", "maxLength", "minLength", "pattern",
				"enum", "multipleOf", "uniqueItems",

				"example", "deprecated", "readOnly", "writeOnly", "nullable", "discriminator", "xml", "externalDocs" };

		for (String prop : commonProperties) {
			if (source.has(prop)) {
				target.set(prop, source.get(prop));
			}
		}
	}

	private static void handleJsonSchemaSpecifics(ObjectNode source, ObjectNode target) {
		Assert.notNull(source, "Source node must not be null");
		Assert.notNull(target, "Target node must not be null");
		if (source.has("properties")) {
			ObjectNode properties = target.putObject("properties");
			source.get("properties").fields().forEachRemaining(entry -> {
				if (entry.getValue() instanceof ObjectNode) {
					properties.set(entry.getKey(), convertSchema((ObjectNode) entry.getValue(),
							JsonParser.getObjectMapper().getNodeFactory()));
				}
			});
		}

		if (source.has("required")) {
			target.set("required", source.get("required"));
		}

		if (source.has("additionalProperties")) {
			JsonNode additionalProps = source.get("additionalProperties");
			if (additionalProps.isBoolean()) {
				target.put("additionalProperties", additionalProps.asBoolean());
			}
			else if (additionalProps.isObject()) {
				target.set("additionalProperties",
						convertSchema((ObjectNode) additionalProps, JsonParser.getObjectMapper().getNodeFactory()));
			}
		}

		if (source.has("items")) {
			JsonNode items = source.get("items");
			if (items.isObject()) {
				target.set("items", convertSchema((ObjectNode) items, JsonParser.getObjectMapper().getNodeFactory()));
			}
		}

		String[] combiners = { "allOf", "anyOf", "oneOf" };
		for (String combiner : combiners) {
			if (source.has(combiner)) {
				JsonNode combinerNode = source.get(combiner);
				if (combinerNode.isArray()) {
					target.putArray(combiner).addAll((com.fasterxml.jackson.databind.node.ArrayNode) combinerNode);
				}
			}
		}
	}

	private static ObjectNode convertSchema(ObjectNode source,
			com.fasterxml.jackson.databind.node.JsonNodeFactory factory) {
		Assert.notNull(source, "Source node must not be null");
		Assert.notNull(factory, "JsonNodeFactory must not be null");

		ObjectNode converted = factory.objectNode();
		copyCommonProperties(source, converted);
		handleJsonSchemaSpecifics(source, converted);
		return converted;
	}

}
