package org.springframework.ai.anthropic;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.anthropic.api.AnthropicApi.ChatCompletionRequest.Metadata;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicChatOptionsTests {

	@Test
	void testBuilderWithAllFields() {
		AnthropicChatOptions options = AnthropicChatOptions.builder()
			.model("test-model")
			.maxTokens(100)
			.stopSequences(List.of("stop1", "stop2"))
			.temperature(0.7)
			.topP(0.8)
			.topK(50)
			.metadata(new Metadata("userId_123"))
			.build();

		assertThat(options).extracting("model", "maxTokens", "stopSequences", "temperature", "topP", "topK", "metadata")
			.containsExactly("test-model", 100, List.of("stop1", "stop2"), 0.7, 0.8, 50, new Metadata("userId_123"));
	}

	@Test
	void testCopy() {
		AnthropicChatOptions original = AnthropicChatOptions.builder()
			.model("test-model")
			.maxTokens(100)
			.stopSequences(List.of("stop1", "stop2"))
			.temperature(0.7)
			.topP(0.8)
			.topK(50)
			.metadata(new Metadata("userId_123"))
			.toolContext(Map.of("key1", "value1"))
			.build();

		AnthropicChatOptions copied = original.copy();

		assertThat(copied).isNotSameAs(original).isEqualTo(original);

		assertThat(copied.getStopSequences()).isNotSameAs(original.getStopSequences());
		assertThat(copied.getToolContext()).isNotSameAs(original.getToolContext());
	}

	@Test
	void testSetters() {
		AnthropicChatOptions options = new AnthropicChatOptions();
		options.setModel("test-model");
		options.setMaxTokens(100);
		options.setTemperature(0.7);
		options.setTopK(50);
		options.setTopP(0.8);
		options.setStopSequences(List.of("stop1", "stop2"));
		options.setMetadata(new Metadata("userId_123"));

		assertThat(options.getModel()).isEqualTo("test-model");
		assertThat(options.getMaxTokens()).isEqualTo(100);
		assertThat(options.getTemperature()).isEqualTo(0.7);
		assertThat(options.getTopK()).isEqualTo(50);
		assertThat(options.getTopP()).isEqualTo(0.8);
		assertThat(options.getStopSequences()).isEqualTo(List.of("stop1", "stop2"));
		assertThat(options.getMetadata()).isEqualTo(new Metadata("userId_123"));
	}

	@Test
	void testDefaultValues() {
		AnthropicChatOptions options = new AnthropicChatOptions();
		assertThat(options.getModel()).isNull();
		assertThat(options.getMaxTokens()).isNull();
		assertThat(options.getTemperature()).isNull();
		assertThat(options.getTopK()).isNull();
		assertThat(options.getTopP()).isNull();
		assertThat(options.getStopSequences()).isNull();
		assertThat(options.getMetadata()).isNull();
	}

}
