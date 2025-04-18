package org.springframework.ai.docker.compose.service.connection.opensearch;

import java.util.Map;

class AwsOpenSearchEnvironment {

	private final String region;

	private final String accessKey;

	private final String secretKey;

	AwsOpenSearchEnvironment(Map<String, String> env) {
		this.region = env.getOrDefault("DEFAULT_REGION", "us-east-1");
		this.accessKey = env.getOrDefault("AWS_ACCESS_KEY_ID", "test");
		this.secretKey = env.getOrDefault("AWS_SECRET_ACCESS_KEY", "test");
	}

	public String getRegion() {
		return this.region;
	}

	public String getAccessKey() {
		return this.accessKey;
	}

	public String getSecretKey() {
		return this.secretKey;
	}

}
