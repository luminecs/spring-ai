package org.springframework.ai.model.chat.memory.jdbc.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(JdbcChatMemoryProperties.CONFIG_PREFIX)
public class JdbcChatMemoryProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.memory.repository.jdbc";

	private boolean initializeSchema = true;

	public boolean isInitializeSchema() {
		return this.initializeSchema;
	}

	public void setInitializeSchema(boolean initializeSchema) {
		this.initializeSchema = initializeSchema;
	}

}
