package org.springframework.ai.chat.metadata;

public interface Usage {

	Integer getPromptTokens();

	Integer getCompletionTokens();

	default Integer getTotalTokens() {
		Integer promptTokens = getPromptTokens();
		promptTokens = promptTokens != null ? promptTokens : 0;
		Integer completionTokens = getCompletionTokens();
		completionTokens = completionTokens != null ? completionTokens : 0;
		return promptTokens + completionTokens;
	}

	Object getNativeUsage();

}
