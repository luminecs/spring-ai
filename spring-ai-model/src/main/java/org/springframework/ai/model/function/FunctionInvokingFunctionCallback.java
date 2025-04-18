package org.springframework.ai.model.function;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.util.Assert;

@Deprecated
public final class FunctionInvokingFunctionCallback<I, O> extends AbstractFunctionCallback<I, O> {

	private final BiFunction<I, ToolContext, O> biFunction;

	FunctionInvokingFunctionCallback(String name, String description, String inputTypeSchema, Type inputType,
			Function<O, String> responseConverter, ObjectMapper objectMapper, BiFunction<I, ToolContext, O> function) {
		super(name, description, inputTypeSchema, inputType, responseConverter, objectMapper);
		Assert.notNull(function, "Function must not be null");
		this.biFunction = function;
	}

	@Override
	public O apply(I input, ToolContext context) {
		return this.biFunction.apply(input, context);
	}

}
