package org.springframework.ai.model.zhipuai.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ZhiPuAiConnectionProperties.CONFIG_PREFIX)
public class ZhiPuAiConnectionProperties extends ZhiPuAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.zhipuai";

	public static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas";

	public ZhiPuAiConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
