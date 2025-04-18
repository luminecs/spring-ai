package org.springframework.ai.model.zhipuai.autoconfigure;

import org.springframework.ai.zhipuai.ZhiPuAiImageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(ZhiPuAiImageProperties.CONFIG_PREFIX)
public class ZhiPuAiImageProperties extends ZhiPuAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.zhipuai.image";

	@NestedConfigurationProperty
	private ZhiPuAiImageOptions options = ZhiPuAiImageOptions.builder().build();

	public ZhiPuAiImageOptions getOptions() {
		return this.options;
	}

	public void setOptions(ZhiPuAiImageOptions options) {
		this.options = options;
	}

}
