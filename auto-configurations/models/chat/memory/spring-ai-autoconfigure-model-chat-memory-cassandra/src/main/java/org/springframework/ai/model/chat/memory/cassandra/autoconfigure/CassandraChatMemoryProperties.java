package org.springframework.ai.model.chat.memory.cassandra.autoconfigure;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.memory.cassandra.CassandraChatMemoryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

@ConfigurationProperties(CassandraChatMemoryProperties.CONFIG_PREFIX)
public class CassandraChatMemoryProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.memory.cassandra";

	private static final Logger logger = LoggerFactory.getLogger(CassandraChatMemoryProperties.class);

	private String keyspace = CassandraChatMemoryConfig.DEFAULT_KEYSPACE_NAME;

	private String table = CassandraChatMemoryConfig.DEFAULT_TABLE_NAME;

	private String assistantColumn = CassandraChatMemoryConfig.DEFAULT_ASSISTANT_COLUMN_NAME;

	private String userColumn = CassandraChatMemoryConfig.DEFAULT_USER_COLUMN_NAME;

	private boolean initializeSchema = true;

	public boolean isInitializeSchema() {
		return this.initializeSchema;
	}

	public void setInitializeSchema(boolean initializeSchema) {
		this.initializeSchema = initializeSchema;
	}

	private Duration timeToLive = null;

	public String getKeyspace() {
		return this.keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public String getTable() {
		return this.table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getAssistantColumn() {
		return this.assistantColumn;
	}

	public void setAssistantColumn(String assistantColumn) {
		this.assistantColumn = assistantColumn;
	}

	public String getUserColumn() {
		return this.userColumn;
	}

	public void setUserColumn(String userColumn) {
		this.userColumn = userColumn;
	}

	@Nullable
	public Duration getTimeToLive() {
		return this.timeToLive;
	}

	public void setTimeToLive(Duration timeToLive) {
		this.timeToLive = timeToLive;
	}

}
