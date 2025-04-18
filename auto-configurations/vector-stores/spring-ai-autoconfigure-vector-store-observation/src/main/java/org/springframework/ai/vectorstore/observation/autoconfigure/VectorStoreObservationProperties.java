package org.springframework.ai.vectorstore.observation.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(VectorStoreObservationProperties.CONFIG_PREFIX)
public class VectorStoreObservationProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.observations";

	private boolean includeQueryResponse = false;

	public boolean isIncludeQueryResponse() {
		return this.includeQueryResponse;
	}

	public void setIncludeQueryResponse(boolean includeQueryResponse) {
		this.includeQueryResponse = includeQueryResponse;
	}

}
