package org.springframework.ai.ollama.api.common;

import org.springframework.ai.observation.conventions.AiProvider;

public final class OllamaApiConstants {

	public static final String DEFAULT_BASE_URL = "http://localhost:11434";

	public static final String PROVIDER_NAME = AiProvider.OLLAMA.value();

	private OllamaApiConstants() {

	}

}
