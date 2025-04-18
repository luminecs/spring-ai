package org.springframework.ai.moonshot.api;

import org.springframework.ai.observation.conventions.AiProvider;

public final class MoonshotConstants {

	public static final String DEFAULT_BASE_URL = "https://api.moonshot.cn";

	public static final String PROVIDER_NAME = AiProvider.MOONSHOT.value();

	private MoonshotConstants() {

	}

}
