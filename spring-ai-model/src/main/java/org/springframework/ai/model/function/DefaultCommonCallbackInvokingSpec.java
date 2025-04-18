package org.springframework.ai.model.function;

import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.springframework.ai.model.function.FunctionCallback.CommonCallbackInvokingSpec;
import org.springframework.ai.model.function.FunctionCallback.SchemaType;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.util.Assert;

@Deprecated
public class DefaultCommonCallbackInvokingSpec<B extends CommonCallbackInvokingSpec<B>>
		implements CommonCallbackInvokingSpec<B> {

	protected String description;

	protected SchemaType schemaType = SchemaType.JSON_SCHEMA;

	protected Function<Object, String> responseConverter = response -> (response instanceof String) ? "" + response
			: this.toJsonString(response);

	protected String inputTypeSchema;

	protected ObjectMapper objectMapper = JsonMapper.builder()
		.addModules(JacksonUtils.instantiateAvailableModules())
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		.build();

	private String toJsonString(Object object) {
		try {
			return this.objectMapper.writeValueAsString(object);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public B description(String description) {
		Assert.hasText(description, "Description must not be empty");
		this.description = description;
		return (B) this;
	}

	@Override
	public B schemaType(SchemaType schemaType) {
		Assert.notNull(schemaType, "SchemaType must not be null");
		this.schemaType = schemaType;
		return (B) this;
	}

	@Override
	public B responseConverter(Function<Object, String> responseConverter) {
		Assert.notNull(responseConverter, "ResponseConverter must not be null");
		this.responseConverter = responseConverter;
		return (B) this;
	}

	@Override
	public B inputTypeSchema(String inputTypeSchema) {
		Assert.hasText(inputTypeSchema, "InputTypeSchema must not be empty");
		this.inputTypeSchema = inputTypeSchema;
		return (B) this;
	}

	@Override
	public B objectMapper(ObjectMapper objectMapper) {
		Assert.notNull(objectMapper, "ObjectMapper must not be null");
		this.objectMapper = objectMapper;
		return (B) this;
	}

	public String getDescription() {
		return this.description;
	}

	public SchemaType getSchemaType() {
		return this.schemaType;
	}

	public Function<Object, String> getResponseConverter() {
		return this.responseConverter;
	}

	public String getInputTypeSchema() {
		return this.inputTypeSchema;
	}

	public ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}
