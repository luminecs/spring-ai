package org.springframework.ai.model.function;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.util.Assert;

@Deprecated
abstract class AbstractFunctionCallback<I, O> implements BiFunction<I, ToolContext, O>, FunctionCallback {

	private final String name;

	private final String description;

	private final Type inputType;

	private final String inputTypeSchema;

	private final ObjectMapper objectMapper;

	private final Function<O, String> responseConverter;

	protected AbstractFunctionCallback(String name, String description, String inputTypeSchema, Type inputType,
			Function<O, String> responseConverter, ObjectMapper objectMapper) {
		Assert.notNull(name, "Name must not be null");
		Assert.notNull(description, "Description must not be null");
		Assert.notNull(inputType, "InputType must not be null");
		Assert.notNull(inputTypeSchema, "InputTypeSchema must not be null");
		Assert.notNull(responseConverter, "ResponseConverter must not be null");
		Assert.notNull(objectMapper, "ObjectMapper must not be null");
		this.name = name;
		this.description = description;
		this.inputType = inputType;
		this.inputTypeSchema = inputTypeSchema;
		this.responseConverter = responseConverter;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getInputTypeSchema() {
		return this.inputTypeSchema;
	}

	@Override
	public String call(String functionInput, ToolContext toolContext) {
		I request = fromJson(functionInput, this.inputType);
		O response = this.apply(request, toolContext);
		return this.responseConverter.apply(response);
	}

	@Override
	public String call(String functionArguments) {

		I request = fromJson(functionArguments, this.inputType);

		return this.andThen(this.responseConverter).apply(request, null);
	}

	private <T> T fromJson(String json, Type targetType) {
		try {
			return this.objectMapper.readValue(json, this.objectMapper.constructType(targetType));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.description, this.inputType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		AbstractFunctionCallback other = (AbstractFunctionCallback) obj;

		return Objects.equals(this.name, other.name) && Objects.equals(this.description, other.description)
				&& Objects.equals(this.inputType, other.inputType);

	}

}
