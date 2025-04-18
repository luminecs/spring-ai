package org.springframework.ai.model.chat.memory.cassandra.autoconfigure;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.memory.cassandra.CassandraChatMemoryConfig;

import static org.assertj.core.api.Assertions.assertThat;

class CassandraChatMemoryPropertiesTest {

	@Test
	void defaultValues() {
		var props = new CassandraChatMemoryProperties();
		assertThat(props.getKeyspace()).isEqualTo(CassandraChatMemoryConfig.DEFAULT_KEYSPACE_NAME);
		assertThat(props.getTable()).isEqualTo(CassandraChatMemoryConfig.DEFAULT_TABLE_NAME);
		assertThat(props.getAssistantColumn()).isEqualTo(CassandraChatMemoryConfig.DEFAULT_ASSISTANT_COLUMN_NAME);
		assertThat(props.getUserColumn()).isEqualTo(CassandraChatMemoryConfig.DEFAULT_USER_COLUMN_NAME);
		assertThat(props.getTimeToLive()).isNull();
		assertThat(props.isInitializeSchema()).isTrue();
	}

	@Test
	void customValues() {
		var props = new CassandraChatMemoryProperties();
		props.setKeyspace("my_keyspace");
		props.setTable("my_table");
		props.setAssistantColumn("my_assistant_column");
		props.setUserColumn("my_user_column");
		props.setTimeToLive(Duration.ofDays(1));
		props.setInitializeSchema(false);

		assertThat(props.getKeyspace()).isEqualTo("my_keyspace");
		assertThat(props.getTable()).isEqualTo("my_table");
		assertThat(props.getAssistantColumn()).isEqualTo("my_assistant_column");
		assertThat(props.getUserColumn()).isEqualTo("my_user_column");
		assertThat(props.getTimeToLive()).isEqualTo(Duration.ofDays(1));
		assertThat(props.isInitializeSchema()).isFalse();
	}

}
