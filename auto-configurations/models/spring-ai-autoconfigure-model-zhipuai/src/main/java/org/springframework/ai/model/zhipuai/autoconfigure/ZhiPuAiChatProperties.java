package org.springframework.ai.model.zhipuai.autoconfigure;

import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(ZhiPuAiChatProperties.CONFIG_PREFIX)
public class ZhiPuAiChatProperties extends ZhiPuAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.zhipuai.chat";

	public static final String DEFAULT_CHAT_MODEL = ZhiPuAiApi.ChatModel.GLM_4_Air.value;

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public ZhiPuAiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(ZhiPuAiChatOptions options) {
		this.options = options;
	}

}
