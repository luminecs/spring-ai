package org.springframework.ai.vectorstore.chroma.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ChromaApiProperties.CONFIG_PREFIX)
public class ChromaApiProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.chroma.client";

	private String host = "http://localhost";

	private int port = 8000;

	private String keyToken;

	private String username;

	private String password;

	public String getHost() {
		return this.host;
	}

	public void setHost(String baseUrl) {
		this.host = baseUrl;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKeyToken() {
		return this.keyToken;
	}

	public void setKeyToken(String keyToken) {
		this.keyToken = keyToken;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
