package org.springframework.ai.chat.memory.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JdbcChatMemoryRepositoryPostgresqlIT.TestConfiguration.class)
@TestPropertySource(properties = "spring.datasource.url=jdbc:tc:postgresql:17:///")
@Sql(scripts = "classpath:org/springframework/ai/chat/memory/jdbc/schema-postgresql.sql")
class JdbcChatMemoryRepositoryPostgresqlIT {

	@Autowired
	private ChatMemoryRepository chatMemoryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void correctChatMemoryRepositoryInstance() {
		assertThat(chatMemoryRepository).isInstanceOf(ChatMemoryRepository.class);
	}

	@ParameterizedTest
	@CsvSource({ "Message from assistant,ASSISTANT", "Message from user,USER", "Message from system,SYSTEM" })
	void saveMessagesSingleMessage(String content, MessageType messageType) {
		var conversationId = UUID.randomUUID().toString();
		var message = switch (messageType) {
			case ASSISTANT -> new AssistantMessage(content + " - " + conversationId);
			case USER -> new UserMessage(content + " - " + conversationId);
			case SYSTEM -> new SystemMessage(content + " - " + conversationId);
			default -> throw new IllegalArgumentException("Type not supported: " + messageType);
		};

		chatMemoryRepository.saveAll(conversationId, List.of(message));

		var query = "SELECT conversation_id, content, type, \"timestamp\" FROM ai_chat_memory WHERE conversation_id = ?";
		var result = jdbcTemplate.queryForMap(query, conversationId);

		assertThat(result.size()).isEqualTo(4);
		assertThat(result.get("conversation_id")).isEqualTo(conversationId);
		assertThat(result.get("content")).isEqualTo(message.getText());
		assertThat(result.get("type")).isEqualTo(messageType.name());
		assertThat(result.get("timestamp")).isInstanceOf(Timestamp.class);
	}

	@Test
	void saveMessagesMultipleMessages() {
		var conversationId = UUID.randomUUID().toString();
		var messages = List.<Message>of(new AssistantMessage("Message from assistant - " + conversationId),
				new UserMessage("Message from user - " + conversationId),
				new SystemMessage("Message from system - " + conversationId));

		chatMemoryRepository.saveAll(conversationId, messages);

		var query = "SELECT conversation_id, content, type, \"timestamp\" FROM ai_chat_memory WHERE conversation_id = ?";
		var results = jdbcTemplate.queryForList(query, conversationId);

		assertThat(results.size()).isEqualTo(messages.size());

		for (var i = 0; i < messages.size(); i++) {
			var message = messages.get(i);
			var result = results.get(i);

			assertThat(result.get("conversation_id")).isNotNull();
			assertThat(result.get("conversation_id")).isEqualTo(conversationId);
			assertThat(result.get("content")).isEqualTo(message.getText());
			assertThat(result.get("type")).isEqualTo(message.getMessageType().name());
			assertThat(result.get("timestamp")).isInstanceOf(Timestamp.class);
		}

		var count = chatMemoryRepository.findByConversationId(conversationId).size();
		assertThat(count).isEqualTo(messages.size());

		chatMemoryRepository.saveAll(conversationId, List.of(new UserMessage("Hello")));

		count = chatMemoryRepository.findByConversationId(conversationId).size();
		assertThat(count).isEqualTo(1);
	}

	@Test
	void findMessagesByConversationId() {
		var conversationId = UUID.randomUUID().toString();
		var messages = List.<Message>of(new AssistantMessage("Message from assistant 1 - " + conversationId),
				new AssistantMessage("Message from assistant 2 - " + conversationId),
				new UserMessage("Message from user - " + conversationId),
				new SystemMessage("Message from system - " + conversationId));

		chatMemoryRepository.saveAll(conversationId, messages);

		var results = chatMemoryRepository.findByConversationId(conversationId);

		assertThat(results.size()).isEqualTo(messages.size());
		assertThat(results).isEqualTo(messages);
	}

	@Test
	void deleteMessagesByConversationId() {
		var conversationId = UUID.randomUUID().toString();
		var messages = List.<Message>of(new AssistantMessage("Message from assistant - " + conversationId),
				new UserMessage("Message from user - " + conversationId),
				new SystemMessage("Message from system - " + conversationId));

		chatMemoryRepository.saveAll(conversationId, messages);

		chatMemoryRepository.deleteByConversationId(conversationId);

		var count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_chat_memory WHERE conversation_id = ?",
				Integer.class, conversationId);

		assertThat(count).isZero();
	}

	@SpringBootConfiguration
	@ImportAutoConfiguration({ DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class })
	static class TestConfiguration {

		@Bean
		ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate) {
			return JdbcChatMemoryRepository.builder().jdbcTemplate(jdbcTemplate).build();
		}

	}

}
