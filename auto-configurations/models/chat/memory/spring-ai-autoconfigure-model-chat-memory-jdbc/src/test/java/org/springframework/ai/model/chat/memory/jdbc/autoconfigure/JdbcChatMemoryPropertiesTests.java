package org.springframework.ai.model.chat.memory.jdbc.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcChatMemoryPropertiesTests {

	@Test
	void defaultValues() {
		var props = new JdbcChatMemoryProperties();

		assertThat(props.isInitializeSchema()).isTrue();
	}

	@Test
	void customValues() {
		var props = new JdbcChatMemoryProperties();
		props.setInitializeSchema(false);

		assertThat(props.isInitializeSchema()).isFalse();
	}

}
