package org.springframework.ai.model.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

@Deprecated
public class MethodInvokingFunctionCallback implements FunctionCallback {

	private static final Logger logger = LoggerFactory.getLogger(MethodInvokingFunctionCallback.class);

	private final Object functionObject;

	private final Method method;

	private final String description;

	private final ObjectMapper mapper;

	private final String inputSchema;

	private boolean isToolContextMethod = false;

	private final String name;

	private final Function<Object, String> responseConverter;

	MethodInvokingFunctionCallback(Object functionObject, Method method, String description, ObjectMapper mapper,
			String name, Function<Object, String> responseConverter) {

		Assert.notNull(method, "Method must not be null");
		Assert.notNull(mapper, "ObjectMapper must not be null");
		Assert.hasText(description, "Description must not be empty");
		Assert.notNull(responseConverter, "Response converter must not be null");

		this.method = method;
		this.description = description;
		this.mapper = mapper;
		this.functionObject = functionObject;
		this.name = name;
		this.responseConverter = responseConverter;

		Assert.isTrue(this.functionObject != null || Modifier.isStatic(this.method.getModifiers()),
				"Function object must be provided for non-static methods!");

		Map<String, Class<?>> methodParameters = Stream.of(method.getParameters())
			.collect(Collectors.toMap(param -> param.getName(), param -> param.getType()));

		this.inputSchema = this.generateJsonSchema(methodParameters);

		logger.debug("Generated JSON Schema: {}", this.inputSchema);
	}

	@Override
	public String getName() {
		return org.springframework.util.StringUtils.hasText(this.name) ? this.name : this.method.getName();
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getInputTypeSchema() {
		return this.inputSchema;
	}

	@Override
	public String call(String functionInput) {
		return this.call(functionInput, null);
	}

	@Override
	public String call(String functionInput, ToolContext toolContext) {

		try {

			if (toolContext != null && !CollectionUtils.isEmpty(toolContext.getContext())
					&& !this.isToolContextMethod) {
				throw new IllegalArgumentException("Configured method does not accept ToolContext as input parameter!");
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> map = this.mapper.readValue(functionInput, Map.class);

			Object[] methodArgs = Stream.of(this.method.getParameters()).map(parameter -> {
				Class<?> type = parameter.getType();
				if (ClassUtils.isAssignable(type, ToolContext.class)) {
					return toolContext;
				}
				Object rawValue = map.get(parameter.getName());
				return this.toJavaType(rawValue, type);
			}).toArray();

			Object response = ReflectionUtils.invokeMethod(this.method, this.functionObject, methodArgs);

			var returnType = this.method.getReturnType();
			if (returnType == Void.TYPE) {
				return "Done";
			}
			else if (returnType == Class.class || returnType.isRecord() || returnType == List.class
					|| returnType == Map.class) {
				return ModelOptionsUtils.toJsonString(response);
			}

			return this.responseConverter.apply(response);
		}
		catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
			return null;
		}
	}

	protected String generateJsonSchema(Map<String, Class<?>> namedClasses) {
		try {
			JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(this.mapper);

			ObjectNode rootNode = this.mapper.createObjectNode();
			rootNode.put("$schema", "https://json-schema.org/draft/2020-12/schema");
			rootNode.put("type", "object");
			ObjectNode propertiesNode = rootNode.putObject("properties");

			for (Map.Entry<String, Class<?>> entry : namedClasses.entrySet()) {
				String className = entry.getKey();
				Class<?> clazz = entry.getValue();

				if (ClassUtils.isAssignable(clazz, ToolContext.class)) {

					this.isToolContextMethod = true;
					continue;
				}

				JsonSchema schema = schemaGen.generateSchema(clazz);
				JsonNode schemaNode = this.mapper.valueToTree(schema);
				propertiesNode.set(className, schemaNode);
			}

			return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Object toJavaType(Object value, Class<?> javaType) {

		if (value == null) {
			return null;
		}

		javaType = ClassUtils.resolvePrimitiveIfNecessary(javaType);

		if (javaType == String.class) {
			return value.toString();
		}
		else if (javaType == Integer.class) {
			return Integer.parseInt(value.toString());
		}
		else if (javaType == Long.class) {
			return Long.parseLong(value.toString());
		}
		else if (javaType == Double.class) {
			return Double.parseDouble(value.toString());
		}
		else if (javaType == Float.class) {
			return Float.parseFloat(value.toString());
		}
		else if (javaType == Boolean.class) {
			return Boolean.parseBoolean(value.toString());
		}
		else if (javaType.isEnum()) {
			return Enum.valueOf((Class<Enum>) javaType, value.toString());
		}

		try {
			String json = this.mapper.writeValueAsString(value);
			return this.mapper.readValue(json, javaType);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
