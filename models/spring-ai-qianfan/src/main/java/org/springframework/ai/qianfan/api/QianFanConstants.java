package org.springframework.ai.qianfan.api;

import org.springframework.ai.observation.conventions.AiProvider;

public final class QianFanConstants {

	public static final String DEFAULT_BASE_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom";

	public static final String PROVIDER_NAME = AiProvider.QIANFAN.value();

	private QianFanConstants() {

	}

}
