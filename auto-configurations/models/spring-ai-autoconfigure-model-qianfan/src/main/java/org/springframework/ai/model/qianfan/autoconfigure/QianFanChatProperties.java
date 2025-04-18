package org.springframework.ai.model.qianfan.autoconfigure;

import org.springframework.ai.qianfan.QianFanChatOptions;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(QianFanChatProperties.CONFIG_PREFIX)
public class QianFanChatProperties extends QianFanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.qianfan.chat";

	public static final String DEFAULT_CHAT_MODEL = QianFanApi.ChatModel.ERNIE_Speed_8K.value;

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private QianFanChatOptions options = QianFanChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public QianFanChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(QianFanChatOptions options) {
		this.options = options;
	}

}
