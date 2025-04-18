package org.springframework.ai.model.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.prompt.ChatOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultFunctionCallingOptionsBuilderTests {

	private DefaultFunctionCallingOptionsBuilder builder;

	@BeforeEach
	void setUp() {
		this.builder = new DefaultFunctionCallingOptionsBuilder();
	}

	@Test
	void shouldBuildWithModel() {

		ChatOptions options = this.builder.model("gpt-4").build();

		assertThat(options.getModel()).isEqualTo("gpt-4");
	}

	@Test
	void shouldBuildWithFrequencyPenalty() {

		ChatOptions options = this.builder.frequencyPenalty(0.5).build();

		assertThat(options.getFrequencyPenalty()).isEqualTo(0.5);
	}

	@Test
	void shouldBuildWithMaxTokens() {

		ChatOptions options = this.builder.maxTokens(100).build();

		assertThat(options.getMaxTokens()).isEqualTo(100);
	}

	@Test
	void shouldBuildWithPresencePenalty() {

		ChatOptions options = this.builder.presencePenalty(0.7).build();

		assertThat(options.getPresencePenalty()).isEqualTo(0.7);
	}

	@Test
	void shouldBuildWithStopSequences() {

		List<String> stopSequences = List.of("stop1", "stop2");

		ChatOptions options = this.builder.stopSequences(stopSequences).build();

		assertThat(options.getStopSequences()).hasSize(2).containsExactlyElementsOf(stopSequences);
	}

	@Test
	void shouldBuildWithTemperature() {

		ChatOptions options = this.builder.temperature(0.8).build();

		assertThat(options.getTemperature()).isEqualTo(0.8);
	}

	@Test
	void shouldBuildWithTopK() {

		ChatOptions options = this.builder.topK(5).build();

		assertThat(options.getTopK()).isEqualTo(5);
	}

	@Test
	void shouldBuildWithTopP() {

		ChatOptions options = this.builder.topP(0.9).build();

		assertThat(options.getTopP()).isEqualTo(0.9);
	}

	@Test
	void shouldBuildWithAllInheritedOptions() {

		ChatOptions options = this.builder.model("gpt-4")
			.frequencyPenalty(0.5)
			.maxTokens(100)
			.presencePenalty(0.7)
			.stopSequences(List.of("stop1", "stop2"))
			.temperature(0.8)
			.topK(5)
			.topP(0.9)
			.build();

		assertThat(options.getModel()).isEqualTo("gpt-4");
		assertThat(options.getFrequencyPenalty()).isEqualTo(0.5);
		assertThat(options.getMaxTokens()).isEqualTo(100);
		assertThat(options.getPresencePenalty()).isEqualTo(0.7);
		assertThat(options.getStopSequences()).containsExactly("stop1", "stop2");
		assertThat(options.getTemperature()).isEqualTo(0.8);
		assertThat(options.getTopK()).isEqualTo(5);
		assertThat(options.getTopP()).isEqualTo(0.9);
	}

	@Test
	void shouldBuildWithFunctionCallbacksList() {

		FunctionCallback callback1 = FunctionCallback.builder()
			.function("test1", (String input) -> "result1")
			.description("Test function 1")
			.inputType(String.class)
			.build();
		FunctionCallback callback2 = FunctionCallback.builder()
			.function("test2", (String input) -> "result2")
			.description("Test function 2")
			.inputType(String.class)
			.build();
		List<FunctionCallback> callbacks = List.of(callback1, callback2);

		FunctionCallingOptions options = this.builder.functionCallbacks(callbacks).build();

		assertThat(options.getFunctionCallbacks()).hasSize(2).containsExactlyElementsOf(callbacks);
	}

	@Test
	void shouldBuildWithFunctionCallbacksVarargs() {

		FunctionCallback callback1 = FunctionCallback.builder()
			.function("test1", (String input) -> "result1")
			.description("Test function 1")
			.inputType(String.class)
			.build();
		FunctionCallback callback2 = FunctionCallback.builder()
			.function("test2", (String input) -> "result2")
			.description("Test function 2")
			.inputType(String.class)
			.build();

		FunctionCallingOptions options = this.builder.functionCallbacks(callback1, callback2).build();

		assertThat(options.getFunctionCallbacks()).hasSize(2).containsExactly(callback1, callback2);
	}

	@Test
	void shouldThrowExceptionWhenFunctionCallbacksVarargsIsNull() {
		assertThatThrownBy(() -> this.builder.functionCallbacks((FunctionCallback[]) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("FunctionCallbacks must not be null");
	}

	@Test
	void shouldBuildWithFunctionsSet() {

		Set<String> functions = Set.of("function1", "function2");

		FunctionCallingOptions options = this.builder.functions(functions).build();

		assertThat(options.getFunctions()).hasSize(2).containsExactlyInAnyOrderElementsOf(functions);
	}

	@Test
	void shouldBuildWithSingleFunction() {

		FunctionCallingOptions options = this.builder.function("function1").function("function2").build();

		assertThat(options.getFunctions()).hasSize(2).containsExactlyInAnyOrder("function1", "function2");
	}

	@Test
	void shouldThrowExceptionWhenFunctionIsNull() {
		assertThatThrownBy(() -> this.builder.function(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Function must not be null");
	}

	@Test
	void shouldBuildWithProxyToolCalls() {

		FunctionCallingOptions options = this.builder.proxyToolCalls(true).build();

		assertThat(options.getProxyToolCalls()).isTrue();
	}

	@Test
	void shouldBuildWithToolContextMap() {

		Map<String, Object> context = Map.of("key1", "value1", "key2", 42);

		FunctionCallingOptions options = this.builder.toolContext(context).build();

		assertThat(options.getToolContext()).hasSize(2).containsAllEntriesOf(context);
	}

	@Test
	void shouldThrowExceptionWhenToolContextMapIsNull() {
		assertThatThrownBy(() -> this.builder.toolContext((Map<String, Object>) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Tool context must not be null");
	}

	@Test
	void shouldBuildWithToolContextKeyValue() {

		FunctionCallingOptions options = this.builder.toolContext("key1", "value1").toolContext("key2", 42).build();

		assertThat(options.getToolContext()).hasSize(2).containsEntry("key1", "value1").containsEntry("key2", 42);
	}

	@Test
	void shouldThrowExceptionWhenToolContextKeyIsNull() {
		assertThatThrownBy(() -> this.builder.toolContext(null, "value")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Key must not be null");
	}

	@Test
	void shouldThrowExceptionWhenToolContextValueIsNull() {
		assertThatThrownBy(() -> this.builder.toolContext("key", null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Value must not be null");
	}

	@Test
	void shouldMergeToolContextMaps() {

		Map<String, Object> context1 = Map.of("key1", "value1", "key2", 42);
		Map<String, Object> context2 = Map.of("key2", "updated", "key3", true);

		FunctionCallingOptions options = this.builder.toolContext(context1).toolContext(context2).build();

		assertThat(options.getToolContext()).hasSize(3)
			.containsEntry("key1", "value1")
			.containsEntry("key2", "updated")
			.containsEntry("key3", true);
	}

	@Test
	void shouldBuildWithAllOptions() {

		FunctionCallback callback = FunctionCallback.builder()
			.function("test", (String input) -> "result")
			.description("Test function")
			.inputType(String.class)
			.build();
		Set<String> functions = Set.of("function1");
		Map<String, Object> context = Map.of("key1", "value1");

		FunctionCallingOptions options = this.builder.model("gpt-4")
			.frequencyPenalty(0.5)
			.maxTokens(100)
			.presencePenalty(0.7)
			.stopSequences(List.of("stop1", "stop2"))
			.temperature(0.8)
			.topK(5)
			.topP(0.9)
			.functionCallbacks(callback)
			.functions(functions)
			.proxyToolCalls(true)
			.toolContext(context)
			.build();

		assertThat(options.getFunctionCallbacks()).hasSize(1).containsExactly(callback);
		assertThat(options.getFunctions()).hasSize(1).containsExactlyElementsOf(functions);
		assertThat(options.getProxyToolCalls()).isTrue();
		assertThat(options.getToolContext()).hasSize(1).containsAllEntriesOf(context);

		ChatOptions chatOptions = options;
		assertThat(chatOptions.getModel()).isEqualTo("gpt-4");
		assertThat(chatOptions.getFrequencyPenalty()).isEqualTo(0.5);
		assertThat(chatOptions.getMaxTokens()).isEqualTo(100);
		assertThat(chatOptions.getPresencePenalty()).isEqualTo(0.7);
		assertThat(chatOptions.getStopSequences()).containsExactly("stop1", "stop2");
		assertThat(chatOptions.getTemperature()).isEqualTo(0.8);
		assertThat(chatOptions.getTopK()).isEqualTo(5);
		assertThat(chatOptions.getTopP()).isEqualTo(0.9);
	}

	@Test
	void shouldBuildWithEmptyFunctionCallbacks() {

		FunctionCallingOptions options = this.builder.functionCallbacks(List.of()).build();

		assertThat(options.getFunctionCallbacks()).isEmpty();
	}

	@Test
	void shouldBuildWithEmptyFunctions() {

		FunctionCallingOptions options = this.builder.functions(Set.of()).build();

		assertThat(options.getFunctions()).isEmpty();
	}

	@Test
	void shouldBuildWithEmptyToolContext() {

		FunctionCallingOptions options = this.builder.toolContext(Map.of()).build();

		assertThat(options.getToolContext()).isEmpty();
	}

	@Test
	void shouldDeduplicateFunctions() {

		FunctionCallingOptions options = this.builder.function("function1")
			.function("function1")
			.function("function2")
			.build();

		assertThat(options.getFunctions()).hasSize(2).containsExactlyInAnyOrder("function1", "function2");
	}

	@Test
	void shouldCopyAllOptions() {

		FunctionCallback callback = FunctionCallback.builder()
			.function("test", (String input) -> "result")
			.description("Test function")
			.inputType(String.class)
			.build();
		FunctionCallingOptions original = this.builder.model("gpt-4")
			.frequencyPenalty(0.5)
			.maxTokens(100)
			.presencePenalty(0.7)
			.stopSequences(List.of("stop1", "stop2"))
			.temperature(0.8)
			.topK(5)
			.topP(0.9)
			.functionCallbacks(callback)
			.function("function1")
			.proxyToolCalls(true)
			.toolContext("key1", "value1")
			.build();

		FunctionCallingOptions copy = original.copy();

		assertThat(copy).usingRecursiveComparison().isEqualTo(original);

		assertThat(copy.getFunctionCallbacks()).isNotSameAs(original.getFunctionCallbacks());
		assertThat(copy.getFunctions()).isNotSameAs(original.getFunctions());
		assertThat(copy.getToolContext()).isNotSameAs(original.getToolContext());
	}

	@Test
	void shouldMergeWithFunctionCallingOptions() {

		FunctionCallback callback1 = FunctionCallback.builder()
			.function("test1", (String input) -> "result1")
			.description("Test function 1")
			.inputType(String.class)
			.build();
		FunctionCallback callback2 = FunctionCallback.builder()
			.function("test2", (String input) -> "result2")
			.description("Test function 2")
			.inputType(String.class)
			.build();

		DefaultFunctionCallingOptions options1 = (DefaultFunctionCallingOptions) this.builder.model("gpt-4")
			.temperature(0.8)
			.functionCallbacks(callback1)
			.function("function1")
			.proxyToolCalls(true)
			.toolContext("key1", "value1")
			.build();

		DefaultFunctionCallingOptions options2 = (DefaultFunctionCallingOptions) FunctionCallingOptions.builder()
			.model("gpt-3.5")
			.maxTokens(100)
			.functionCallbacks(callback2)
			.function("function2")
			.proxyToolCalls(false)
			.toolContext("key2", "value2")
			.build();

		FunctionCallingOptions merged = options1.merge(options2);

		assertThat(merged.getModel()).isEqualTo("gpt-3.5");
		assertThat(merged.getTemperature()).isEqualTo(0.8);
		assertThat(merged.getMaxTokens()).isEqualTo(100);
		assertThat(merged.getFunctionCallbacks()).containsExactly(callback1, callback2);
		assertThat(merged.getFunctions()).containsExactlyInAnyOrder("function1", "function2");
		assertThat(merged.getProxyToolCalls()).isFalse();
		assertThat(merged.getToolContext()).containsEntry("key1", "value1").containsEntry("key2", "value2");
	}

	@Test
	void shouldMergeWithChatOptions() {

		FunctionCallback callback = FunctionCallback.builder()
			.function("test", (String input) -> "result")
			.description("Test function")
			.inputType(String.class)
			.build();

		DefaultFunctionCallingOptions options1 = (DefaultFunctionCallingOptions) this.builder.model("gpt-4")
			.temperature(0.8)
			.functionCallbacks(callback)
			.function("function1")
			.proxyToolCalls(true)
			.toolContext("key1", "value1")
			.build();

		ChatOptions options2 = ChatOptions.builder().model("gpt-3.5").maxTokens(100).build();

		FunctionCallingOptions merged = options1.merge(options2);

		assertThat(merged.getModel()).isEqualTo("gpt-3.5");
		assertThat(merged.getTemperature()).isEqualTo(0.8);
		assertThat(merged.getMaxTokens()).isEqualTo(100);

		assertThat(merged.getFunctionCallbacks()).containsExactly(callback);
		assertThat(merged.getFunctions()).containsExactly("function1");
		assertThat(merged.getProxyToolCalls()).isTrue();
		assertThat(merged.getToolContext()).containsEntry("key1", "value1");
	}

	@Test
	void shouldAllowBuilderReuse() {

		FunctionCallback callback1 = FunctionCallback.builder()
			.function("test1", (String input) -> "result1")
			.description("Test function 1")
			.inputType(String.class)
			.build();
		FunctionCallback callback2 = FunctionCallback.builder()
			.function("test2", (String input) -> "result2")
			.description("Test function 2")
			.inputType(String.class)
			.build();

		FunctionCallingOptions options1 = this.builder.model("model1")
			.temperature(0.7)
			.functionCallbacks(callback1)
			.build();

		FunctionCallingOptions options2 = this.builder.model("model2").functionCallbacks(callback2).build();

		assertThat(options1.getModel()).isEqualTo("model1");
		assertThat(options1.getTemperature()).isEqualTo(0.7);
		assertThat(options1.getFunctionCallbacks()).containsExactly(callback1);

		assertThat(options2.getModel()).isEqualTo("model2");
		assertThat(options2.getTemperature()).isEqualTo(0.7);
		assertThat(options2.getFunctionCallbacks()).containsExactly(callback2);

	}

	@Test
	void shouldReturnSameBuilderInstanceOnEachMethod() {

		FunctionCallingOptions.Builder returnedBuilder = this.builder.model("test");

		assertThat(returnedBuilder).isSameAs(this.builder);
	}

	@Test
	void shouldHaveExpectedDefaultValues() {

		FunctionCallingOptions options = this.builder.build();

		assertThat(options.getModel()).isNull();
		assertThat(options.getTemperature()).isNull();
		assertThat(options.getMaxTokens()).isNull();
		assertThat(options.getTopP()).isNull();
		assertThat(options.getTopK()).isNull();
		assertThat(options.getFrequencyPenalty()).isNull();
		assertThat(options.getPresencePenalty()).isNull();
		assertThat(options.getStopSequences()).isNull();

		assertThat(options.getFunctionCallbacks()).isEmpty();
		assertThat(options.getFunctions()).isEmpty();
		assertThat(options.getToolContext()).isEmpty();
		assertThat(options.getProxyToolCalls()).isFalse();
	}

	@Test
	void shouldBeImmutableAfterBuild() {

		FunctionCallback callback = FunctionCallback.builder()
			.function("test", (String input) -> "result")
			.description("Test function")
			.inputType(String.class)
			.build();

		List<String> stopSequences = new ArrayList<>(List.of("stop1", "stop2"));
		Set<String> functions = new HashSet<>(Set.of("function1", "function2"));
		Map<String, Object> context = new HashMap<>(Map.of("key1", "value1"));

		FunctionCallingOptions options = this.builder.stopSequences(stopSequences)
			.functionCallbacks(callback)
			.functions(functions)
			.toolContext(context)
			.build();

		assertThatThrownBy(() -> options.getStopSequences().add("stop3"))
			.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> options.getFunctionCallbacks().add(callback))
			.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> options.getFunctions().add("function3"))
			.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> options.getToolContext().put("key2", "value2"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

}
