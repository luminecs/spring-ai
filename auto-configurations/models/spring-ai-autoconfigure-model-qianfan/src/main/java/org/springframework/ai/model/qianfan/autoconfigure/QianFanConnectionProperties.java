package org.springframework.ai.model.qianfan.autoconfigure;

import org.springframework.ai.qianfan.api.QianFanConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(QianFanConnectionProperties.CONFIG_PREFIX)
public class QianFanConnectionProperties extends QianFanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.qianfan";

	public static final String DEFAULT_BASE_URL = QianFanConstants.DEFAULT_BASE_URL;

	public QianFanConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
