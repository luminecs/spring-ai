package org.springframework.ai.zhipuai.api;

import org.springframework.ai.observation.conventions.AiProvider;

public final class ZhiPuApiConstants {

	public static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas";

	public static final String PROVIDER_NAME = AiProvider.ZHIPUAI.value();

	private ZhiPuApiConstants() {

	}

}
