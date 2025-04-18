package org.springframework.ai.model.chat.memory.neo4j.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.memory.neo4j.Neo4jChatMemoryConfig;

import static org.assertj.core.api.Assertions.assertThat;

class Neo4jChatMemoryPropertiesTest {

	@Test
	void defaultValues() {
		var props = new Neo4jChatMemoryProperties();
		assertThat(props.getMediaLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_MEDIA_LABEL);
		assertThat(props.getMessageLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_MESSAGE_LABEL);
		assertThat(props.getMetadataLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_METADATA_LABEL);
		assertThat(props.getSessionLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_SESSION_LABEL);
		assertThat(props.getToolCallLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_TOOL_CALL_LABEL);
		assertThat(props.getToolResponseLabel()).isEqualTo(Neo4jChatMemoryConfig.DEFAULT_TOOL_RESPONSE_LABEL);
	}

}
