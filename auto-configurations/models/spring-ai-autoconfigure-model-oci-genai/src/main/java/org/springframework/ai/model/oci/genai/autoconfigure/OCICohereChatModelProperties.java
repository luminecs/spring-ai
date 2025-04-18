package org.springframework.ai.model.oci.genai.autoconfigure;

import org.springframework.ai.oci.cohere.OCICohereChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OCICohereChatModelProperties.CONFIG_PREFIX)
public class OCICohereChatModelProperties {

	public static final String CONFIG_PREFIX = "spring.ai.oci.genai.cohere.chat";

	private static final String DEFAULT_SERVING_MODE = ServingMode.ON_DEMAND.getMode();

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private OCICohereChatOptions options = OCICohereChatOptions.builder()
		.servingMode(DEFAULT_SERVING_MODE)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public OCICohereChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(OCICohereChatOptions options) {
		this.options = options;
	}

}
