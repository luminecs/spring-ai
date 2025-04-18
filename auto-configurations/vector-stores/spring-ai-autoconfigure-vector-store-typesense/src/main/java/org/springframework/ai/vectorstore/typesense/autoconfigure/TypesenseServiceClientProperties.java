package org.springframework.ai.vectorstore.typesense.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(TypesenseServiceClientProperties.CONFIG_PREFIX)
public class TypesenseServiceClientProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.typesense.client";

	private String protocol = "http";

	private String host = "localhost";

	private int port = 8108;

	private String apiKey = "xyz";

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}
