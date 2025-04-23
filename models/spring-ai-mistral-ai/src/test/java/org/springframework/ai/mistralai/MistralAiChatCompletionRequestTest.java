package org.springframework.ai.mistralai;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MistralAiTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".+")
public class MistralAiChatCompletionRequestTest {

	MistralAiChatModel chatModel = MistralAiChatModel.builder().mistralAiApi(new MistralAiApi("test")).build();

	@Test
	void chatCompletionDefaultRequestTest() {
		var prompt = this.chatModel.buildRequestPrompt(new Prompt("test content"));
		var request = this.chatModel.createRequest(prompt, false);

		assertThat(request.messages()).hasSize(1);
		assertThat(request.topP()).isEqualTo(1);
		assertThat(request.temperature()).isEqualTo(0.7);
		assertThat(request.safePrompt()).isFalse();
		assertThat(request.maxTokens()).isNull();
		assertThat(request.stream()).isFalse();
	}

	@Test
	void chatCompletionRequestWithOptionsTest() {
		var options = MistralAiChatOptions.builder().temperature(0.5).topP(0.8).build();
		var prompt = this.chatModel.buildRequestPrompt(new Prompt("test content", options));
		var request = this.chatModel.createRequest(prompt, true);

		assertThat(request.messages().size()).isEqualTo(1);
		assertThat(request.topP()).isEqualTo(0.8);
		assertThat(request.temperature()).isEqualTo(0.5);
		assertThat(request.stream()).isTrue();
	}

	@Test
	void whenToolRuntimeOptionsThenMergeWithDefaults() {
		MistralAiChatOptions defaultOptions = MistralAiChatOptions.builder()
			.model("DEFAULT_MODEL")
			.internalToolExecutionEnabled(true)
			.toolCallbacks(new TestToolCallback("tool1"), new TestToolCallback("tool2"))
			.toolNames("tool1", "tool2")
			.toolContext(Map.of("key1", "value1", "key2", "valueA"))
			.build();

		MistralAiChatModel chatModel = MistralAiChatModel.builder()
			.mistralAiApi(new MistralAiApi("test"))
			.defaultOptions(defaultOptions)
			.build();

		MistralAiChatOptions runtimeOptions = MistralAiChatOptions.builder()
			.internalToolExecutionEnabled(false)
			.toolCallbacks(new TestToolCallback("tool3"), new TestToolCallback("tool4"))
			.toolNames("tool3")
			.toolContext(Map.of("key2", "valueB"))
			.build();
		Prompt prompt = chatModel.buildRequestPrompt(new Prompt("Test message content", runtimeOptions));

		assertThat(((ToolCallingChatOptions) prompt.getOptions())).isNotNull();
		assertThat(((ToolCallingChatOptions) prompt.getOptions()).getInternalToolExecutionEnabled()).isFalse();
		assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()).hasSize(2);
		assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolCallbacks()
			.stream()
			.map(toolCallback -> toolCallback.getToolDefinition().name())).containsExactlyInAnyOrder("tool3", "tool4");
		assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolNames()).containsExactlyInAnyOrder("tool3");
		assertThat(((ToolCallingChatOptions) prompt.getOptions()).getToolContext()).containsEntry("key1", "value1")
			.containsEntry("key2", "valueB");
	}

	static class TestToolCallback implements ToolCallback {

		private final ToolDefinition toolDefinition;

		TestToolCallback(String name) {
			this.toolDefinition = ToolDefinition.builder().name(name).inputSchema("{}").build();
		}

		@Override
		public ToolDefinition getToolDefinition() {
			return this.toolDefinition;
		}

		@Override
		public String call(String toolInput) {
			return "Mission accomplished!";
		}

	}

}
