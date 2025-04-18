package org.springframework.ai.chat.metadata;

import java.util.Map;

public class EmptyUsage implements Usage {

	@Override
	public Integer getPromptTokens() {
		return 0;
	}

	@Override
	public Integer getCompletionTokens() {
		return 0;
	}

	@Override
	public Object getNativeUsage() {
		return Map.of();
	}

}
