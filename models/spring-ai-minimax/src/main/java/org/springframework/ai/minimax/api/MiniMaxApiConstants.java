package org.springframework.ai.minimax.api;

import org.springframework.ai.observation.conventions.AiProvider;

public final class MiniMaxApiConstants {

	public static final String DEFAULT_BASE_URL = "https://api.minimax.chat";

	public static final String TOOL_CALL_FUNCTION_TYPE = "function";

	public static final String PROVIDER_NAME = AiProvider.MINIMAX.value();

	private MiniMaxApiConstants() {

	}

}
