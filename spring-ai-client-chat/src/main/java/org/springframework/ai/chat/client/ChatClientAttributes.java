package org.springframework.ai.chat.client;

public enum ChatClientAttributes {

	OUTPUT_FORMAT("spring.ai.chat.client.output.format");

	private final String key;

	ChatClientAttributes(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
