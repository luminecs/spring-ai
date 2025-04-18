package org.springframework.ai.model.qianfan.autoconfigure;

import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(QianFanImageProperties.CONFIG_PREFIX)
public class QianFanImageProperties extends QianFanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.qianfan.image";

	public static final String DEFAULT_IMAGE_MODEL = QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue();

	@NestedConfigurationProperty
	private QianFanImageOptions options = QianFanImageOptions.builder().model(DEFAULT_IMAGE_MODEL).build();

	public QianFanImageOptions getOptions() {
		return this.options;
	}

	public void setOptions(QianFanImageOptions options) {
		this.options = options;
	}

}
