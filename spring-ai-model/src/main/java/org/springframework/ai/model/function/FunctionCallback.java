package org.springframework.ai.model.function;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.core.ParameterizedTypeReference;

@Deprecated
public interface FunctionCallback {

	String getName();

	String getDescription();

	String getInputTypeSchema();

	String call(String functionInput);

	default String call(String functionInput, ToolContext toolContext) {
		if (toolContext != null && !toolContext.getContext().isEmpty()) {
			throw new UnsupportedOperationException("Function context is not supported!");
		}
		return call(functionInput);
	}

	static Builder builder() {
		return new DefaultFunctionCallbackBuilder();
	}

	enum SchemaType {

		JSON_SCHEMA,

		OPEN_API_SCHEMA

	}

	@Deprecated
	interface Builder {

		<I, O> FunctionInvokingSpec<I, O> function(String name, Function<I, O> function);

		<I, O> FunctionInvokingSpec<I, O> function(String name, BiFunction<I, ToolContext, O> biFunction);

		<O> FunctionInvokingSpec<Void, O> function(String name, Supplier<O> supplier);

		<I> FunctionInvokingSpec<I, Void> function(String name, Consumer<I> consumer);

		MethodInvokingSpec method(String methodName, Class<?>... argumentTypes);

	}

	interface CommonCallbackInvokingSpec<B extends CommonCallbackInvokingSpec<B>> {

		B description(String description);

		B schemaType(SchemaType schemaType);

		B responseConverter(Function<Object, String> responseConverter);

		B inputTypeSchema(String inputTypeSchema);

		B objectMapper(ObjectMapper objectMapper);

	}

	interface FunctionInvokingSpec<I, O> extends CommonCallbackInvokingSpec<FunctionInvokingSpec<I, O>> {

		FunctionInvokingSpec<I, O> inputType(Class<?> inputType);

		FunctionInvokingSpec<I, O> inputType(ParameterizedTypeReference<?> inputType);

		FunctionCallback build();

	}

	interface MethodInvokingSpec extends CommonCallbackInvokingSpec<MethodInvokingSpec> {

		MethodInvokingSpec name(String name);

		MethodInvokingSpec targetObject(Object methodObject);

		MethodInvokingSpec targetClass(Class<?> targetClass);

		FunctionCallback build();

	}

}
