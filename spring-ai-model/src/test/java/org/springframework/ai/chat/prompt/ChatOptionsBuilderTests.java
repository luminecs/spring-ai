package org.springframework.ai.chat.prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.function.FunctionToolCallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ChatOptionsBuilderTests {

	private ChatOptions.Builder builder;

	@BeforeEach
	void setUp() {
		this.builder = ChatOptions.builder();
	}

	@Test
	void shouldBuildWithAllOptions() {
		ChatOptions options = this.builder.model("gpt-4")
			.maxTokens(100)
			.temperature(0.7)
			.topP(1.0)
			.topK(40)
			.stopSequences(List.of("stop1", "stop2"))
			.build();

		assertThat(options.getModel()).isEqualTo("gpt-4");
		assertThat(options.getMaxTokens()).isEqualTo(100);
		assertThat(options.getTemperature()).isEqualTo(0.7);
		assertThat(options.getTopP()).isEqualTo(1.0);
		assertThat(options.getTopK()).isEqualTo(40);
		assertThat(options.getStopSequences()).containsExactly("stop1", "stop2");
	}

	@Test
	void shouldBuildWithMinimalOptions() {
		ChatOptions options = this.builder.model("gpt-4").build();

		assertThat(options.getModel()).isEqualTo("gpt-4");
		assertThat(options.getMaxTokens()).isNull();
		assertThat(options.getTemperature()).isNull();
		assertThat(options.getTopP()).isNull();
		assertThat(options.getTopK()).isNull();
		assertThat(options.getStopSequences()).isNull();
	}

	@Test
	void shouldCopyOptions() {
		ChatOptions original = this.builder.model("gpt-4")
			.maxTokens(100)
			.temperature(0.7)
			.topP(1.0)
			.topK(40)
			.stopSequences(List.of("stop1", "stop2"))
			.build();

		ChatOptions copy = original.copy();

		assertThat(copy).usingRecursiveComparison().isEqualTo(original);

		assertThat(copy.getStopSequences()).isNotSameAs(original.getStopSequences());
	}

	@Test
	void shouldUpcastToChatOptions() {

		FunctionToolCallback callback = FunctionToolCallback.builder("function1", x -> "result")
			.description("Test function")
			.inputType(String.class)
			.build();

		ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
			.model("gpt-4")
			.maxTokens(100)
			.temperature(0.7)
			.topP(1.0)
			.topK(40)
			.stopSequences(List.of("stop1", "stop2"))
			.toolNames(Set.of("function1", "function2"))
			.toolCallbacks(List.of(callback))
			.build();

		ChatOptions chatOptions = toolCallingChatOptions;

		assertThat(chatOptions.getModel()).isEqualTo("gpt-4");
		assertThat(chatOptions.getMaxTokens()).isEqualTo(100);
		assertThat(chatOptions.getTemperature()).isEqualTo(0.7);
		assertThat(chatOptions.getTopP()).isEqualTo(1.0);
		assertThat(chatOptions.getTopK()).isEqualTo(40);
		assertThat(chatOptions.getStopSequences()).containsExactly("stop1", "stop2");
	}

	@Test
	void shouldAllowBuilderReuse() {

		ChatOptions options1 = this.builder.model("model1").temperature(0.7).build();
		ChatOptions options2 = this.builder.model("model2").build();

		assertThat(options1.getModel()).isEqualTo("model1");
		assertThat(options1.getTemperature()).isEqualTo(0.7);
		assertThat(options2.getModel()).isEqualTo("model2");
		assertThat(options2.getTemperature()).isEqualTo(0.7);
	}

	@Test
	void shouldReturnSameBuilderInstanceOnEachMethod() {

		ChatOptions.Builder returnedBuilder = this.builder.model("test");

		assertThat(returnedBuilder).isSameAs(this.builder);
	}

	@Test
	void shouldHaveExpectedDefaultValues() {

		ChatOptions options = this.builder.build();

		assertThat(options.getModel()).isNull();
		assertThat(options.getTemperature()).isNull();
		assertThat(options.getMaxTokens()).isNull();
		assertThat(options.getTopP()).isNull();
		assertThat(options.getTopK()).isNull();
		assertThat(options.getFrequencyPenalty()).isNull();
		assertThat(options.getPresencePenalty()).isNull();
		assertThat(options.getStopSequences()).isNull();
	}

	@Test
	void shouldBeImmutableAfterBuild() {

		List<String> stopSequences = new ArrayList<>(List.of("stop1", "stop2"));
		ChatOptions options = this.builder.stopSequences(stopSequences).build();

		assertThatThrownBy(() -> options.getStopSequences().add("stop3"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

}
