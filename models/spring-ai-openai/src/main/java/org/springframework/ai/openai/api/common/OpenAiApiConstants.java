package org.springframework.ai.openai.api.common;

import org.springframework.ai.observation.conventions.AiProvider;

public final class OpenAiApiConstants {

	public static final String DEFAULT_BASE_URL = "https://api.openai.com";

	public static final String PROVIDER_NAME = AiProvider.OPENAI.value();

	private OpenAiApiConstants() {

	}

}
