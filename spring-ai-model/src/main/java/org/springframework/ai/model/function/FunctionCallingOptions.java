package org.springframework.ai.model.function;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

@Deprecated
public interface FunctionCallingOptions extends ChatOptions {

	static Builder builder() {
		return new DefaultFunctionCallingOptionsBuilder();
	}

	List<FunctionCallback> getFunctionCallbacks();

	void setFunctionCallbacks(List<FunctionCallback> functionCallbacks);

	Set<String> getFunctions();

	void setFunctions(Set<String> functions);

	default Boolean getProxyToolCalls() {
		return false;
	}

	default void setProxyToolCalls(Boolean proxyToolCalls) {
		if (proxyToolCalls != null) {
			throw new UnsupportedOperationException("Setting Proxy Tool Calls are not supported!");
		}
	}

	Map<String, Object> getToolContext();

	void setToolContext(Map<String, Object> tooContext);

	interface Builder extends ChatOptions.Builder {

		Builder functionCallbacks(List<FunctionCallback> functionCallbacks);

		Builder functionCallbacks(FunctionCallback... functionCallbacks);

		Builder functions(Set<String> functions);

		Builder function(String function);

		Builder proxyToolCalls(Boolean proxyToolCalls);

		Builder toolContext(Map<String, Object> context);

		Builder toolContext(String key, Object value);

		@Override
		FunctionCallingOptions build();

		@Override
		Builder model(String model);

		@Override
		Builder frequencyPenalty(Double frequencyPenalty);

		@Override
		Builder maxTokens(Integer maxTokens);

		@Override
		Builder presencePenalty(Double presencePenalty);

		@Override
		Builder stopSequences(List<String> stopSequences);

		@Override
		Builder temperature(Double temperature);

		@Override
		Builder topK(Integer topK);

		@Override
		Builder topP(Double topP);

	}

}
