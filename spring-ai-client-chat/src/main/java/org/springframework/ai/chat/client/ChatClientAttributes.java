package org.springframework.ai.chat.client;

public enum ChatClientAttributes {

	@Deprecated
	ADVISORS("spring.ai.chat.client.advisors"), @Deprecated
	CHAT_MODEL("spring.ai.chat.client.model"), @Deprecated
	OUTPUT_FORMAT("spring.ai.chat.client.output.format"), @Deprecated
	USER_PARAMS("spring.ai.chat.client.user.params"), @Deprecated
	SYSTEM_PARAMS("spring.ai.chat.client.system.params");

	private final String key;

	ChatClientAttributes(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
