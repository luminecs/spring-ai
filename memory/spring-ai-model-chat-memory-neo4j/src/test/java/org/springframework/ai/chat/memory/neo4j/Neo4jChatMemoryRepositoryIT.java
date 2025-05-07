package org.springframework.ai.chat.memory.neo4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Neo4jChatMemoryRepositoryIT {

	static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("neo4j");

	@SuppressWarnings({ "rawtypes", "resource" })
	@Container
	static Neo4jContainer neo4jContainer = (Neo4jContainer) new Neo4jContainer(DEFAULT_IMAGE_NAME.withTag("5"))
		.withoutAuthentication()
		.withExposedPorts(7474, 7687);

	private ChatMemoryRepository chatMemoryRepository;

	private Driver driver;

	private Neo4jChatMemoryConfig config;

	@BeforeEach
	void setUp() {
		driver = Neo4jDriverFactory.create(neo4jContainer.getBoltUrl());
		config = Neo4jChatMemoryConfig.builder().withDriver(driver).build();
		chatMemoryRepository = new Neo4jChatMemoryRepository(config);
	}

	@AfterEach
	void tearDown() {

		try (Session session = driver.session()) {
			session.run("MATCH (n) DETACH DELETE n");
		}
		driver.close();
	}

	@Test
	void correctChatMemoryRepositoryInstance() {
		assertThat(chatMemoryRepository).isInstanceOf(ChatMemoryRepository.class);
		assertThat(chatMemoryRepository).isInstanceOf(Neo4jChatMemoryRepository.class);
	}

	@ParameterizedTest
	@CsvSource({ "Message from assistant,ASSISTANT", "Message from user,USER", "Message from system,SYSTEM",
			"Message from tool,TOOL" })
	void saveAndFindSingleMessage(String content, MessageType messageType) {
		var conversationId = UUID.randomUUID().toString();
		Message message = createMessageByType(content + " - " + conversationId, messageType);

		chatMemoryRepository.saveAll(conversationId, List.<Message>of(message));
		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);

		assertThat(retrievedMessages).hasSize(1);

		Message retrievedMessage = retrievedMessages.get(0);
		assertThat(retrievedMessage.getMessageType()).isEqualTo(messageType);

		if (messageType != MessageType.TOOL) {
			assertThat(retrievedMessage.getText()).isEqualTo(message.getText());
		}

		try (Session session = driver.session()) {
			var result = session.run(
					"MATCH (s:%s {id:$conversationId})-[:HAS_MESSAGE]->(m:%s) RETURN count(m) as count"
						.formatted(config.getSessionLabel(), config.getMessageLabel()),
					Map.of("conversationId", conversationId));
			assertThat(result.single().get("count").asLong()).isEqualTo(1);
		}
	}

	@Test
	void saveAndFindMultipleMessages() {
		var conversationId = UUID.randomUUID().toString();
		List<Message> messages = List.of(new AssistantMessage("Message from assistant - " + conversationId),
				new UserMessage("Message from user - " + conversationId),
				new SystemMessage("Message from system - " + conversationId),
				new ToolResponseMessage(List.of(new ToolResponse("id", "name", "responseData"))));

		chatMemoryRepository.saveAll(conversationId, messages);
		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);

		assertThat(retrievedMessages).hasSize(messages.size());

		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getMessageType() != MessageType.TOOL) {
				assertThat(retrievedMessages.get(i).getText()).isEqualTo(messages.get(i).getText());
			}
			assertThat(retrievedMessages.get(i).getMessageType()).isEqualTo(messages.get(i).getMessageType());
		}
	}

	@Test
	void verifyMessageOrdering() {
		var conversationId = UUID.randomUUID().toString();
		List<Message> messages = new ArrayList<>();

		for (int i = 1; i <= 5; i++) {
			messages.add(new UserMessage("Message " + i));
		}

		chatMemoryRepository.saveAll(conversationId, messages);
		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);

		assertThat(retrievedMessages).hasSize(messages.size());

		for (int i = 0; i < messages.size(); i++) {
			assertThat(retrievedMessages.get(i).getText()).isEqualTo("Message " + (i + 1));
		}
	}

	@Test
	void findConversationIds() {

		var conversationId1 = UUID.randomUUID().toString();
		var conversationId2 = UUID.randomUUID().toString();
		var conversationId3 = UUID.randomUUID().toString();

		chatMemoryRepository.saveAll(conversationId1, List.<Message>of(new UserMessage("Message for conversation 1")));
		chatMemoryRepository.saveAll(conversationId2, List.<Message>of(new UserMessage("Message for conversation 2")));
		chatMemoryRepository.saveAll(conversationId3, List.<Message>of(new UserMessage("Message for conversation 3")));

		List<String> conversationIds = chatMemoryRepository.findConversationIds();

		assertThat(conversationIds).hasSize(3);
		assertThat(conversationIds).contains(conversationId1, conversationId2, conversationId3);
	}

	@Test
	void deleteByConversationId() {
		var conversationId = UUID.randomUUID().toString();
		List<Message> messages = List.of(new AssistantMessage("Message from assistant"),
				new UserMessage("Message from user"), new SystemMessage("Message from system"));

		chatMemoryRepository.saveAll(conversationId, messages);

		assertThat(chatMemoryRepository.findByConversationId(conversationId)).hasSize(3);

		chatMemoryRepository.deleteByConversationId(conversationId);

		assertThat(chatMemoryRepository.findByConversationId(conversationId)).isEmpty();

		try (Session session = driver.session()) {
			var result = session.run(
					"MATCH (s:%s {id:$conversationId}) RETURN count(s) as count".formatted(config.getSessionLabel()),
					Map.of("conversationId", conversationId));
			assertThat(result.single().get("count").asLong()).isZero();
		}
	}

	@Test
	void saveAllReplacesExistingMessages() {
		var conversationId = UUID.randomUUID().toString();

		List<Message> initialMessages = List.of(new UserMessage("Initial message 1"),
				new UserMessage("Initial message 2"), new UserMessage("Initial message 3"));
		chatMemoryRepository.saveAll(conversationId, initialMessages);

		assertThat(chatMemoryRepository.findByConversationId(conversationId)).hasSize(3);

		List<Message> newMessages = List.of(new UserMessage("New message 1"), new UserMessage("New message 2"));
		chatMemoryRepository.saveAll(conversationId, newMessages);

		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(retrievedMessages).hasSize(2);
		assertThat(retrievedMessages.get(0).getText()).isEqualTo("New message 1");
		assertThat(retrievedMessages.get(1).getText()).isEqualTo("New message 2");
	}

	@Test
	void handleMediaContent() {
		var conversationId = UUID.randomUUID().toString();

		MimeType textPlain = MimeType.valueOf("text/plain");
		List<Media> media = List.of(Media.builder()
			.name("some media")
			.id(UUID.randomUUID().toString())
			.mimeType(textPlain)
			.data("hello".getBytes(StandardCharsets.UTF_8))
			.build(), Media.builder().data(URI.create("http://www.example.com")).mimeType(textPlain).build());

		UserMessage userMessageWithMedia = UserMessage.builder().text("Message with media").media(media).build();

		chatMemoryRepository.saveAll(conversationId, List.<Message>of(userMessageWithMedia));

		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(retrievedMessages).hasSize(1);

		UserMessage retrievedMessage = (UserMessage) retrievedMessages.get(0);
		assertThat(retrievedMessage.getMedia()).hasSize(2);
		assertThat(retrievedMessage.getMedia()).usingRecursiveFieldByFieldElementComparator().isEqualTo(media);
	}

	@Test
	void handleAssistantMessageWithToolCalls() {
		var conversationId = UUID.randomUUID().toString();

		AssistantMessage assistantMessage = new AssistantMessage("Message with tool calls", Map.of(),
				List.of(new AssistantMessage.ToolCall("id1", "type1", "name1", "arguments1"),
						new AssistantMessage.ToolCall("id2", "type2", "name2", "arguments2")));

		chatMemoryRepository.saveAll(conversationId, List.<Message>of(assistantMessage));

		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(retrievedMessages).hasSize(1);

		AssistantMessage retrievedMessage = (AssistantMessage) retrievedMessages.get(0);
		assertThat(retrievedMessage.getToolCalls()).hasSize(2);
		assertThat(retrievedMessage.getToolCalls().get(0).id()).isEqualTo("id1");
		assertThat(retrievedMessage.getToolCalls().get(1).id()).isEqualTo("id2");
	}

	@Test
	void handleToolResponseMessage() {
		var conversationId = UUID.randomUUID().toString();

		ToolResponseMessage toolResponseMessage = new ToolResponseMessage(List
			.of(new ToolResponse("id1", "name1", "responseData1"), new ToolResponse("id2", "name2", "responseData2")),
				Map.of("metadataKey", "metadataValue"));

		chatMemoryRepository.saveAll(conversationId, List.<Message>of(toolResponseMessage));

		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(retrievedMessages).hasSize(1);

		ToolResponseMessage retrievedMessage = (ToolResponseMessage) retrievedMessages.get(0);
		assertThat(retrievedMessage.getResponses()).hasSize(2);
		assertThat(retrievedMessage.getResponses().get(0).id()).isEqualTo("id1");
		assertThat(retrievedMessage.getResponses().get(1).id()).isEqualTo("id2");
		assertThat(retrievedMessage.getMetadata()).containsEntry("metadataKey", "metadataValue");
	}

	@Test
	void saveAndFindSystemMessageWithMetadata() {
		var conversationId = UUID.randomUUID().toString();
		Map<String, Object> customMetadata = Map.of("priority", "high", "source", "test");

		SystemMessage systemMessage = SystemMessage.builder()
			.text("System message with custom metadata - " + conversationId)
			.metadata(customMetadata)
			.build();

		chatMemoryRepository.saveAll(conversationId, List.of(systemMessage));
		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);

		assertThat(retrievedMessages).hasSize(1);
		Message retrievedMessage = retrievedMessages.get(0);

		assertThat(retrievedMessage).isInstanceOf(SystemMessage.class);
		assertThat(retrievedMessage.getText()).isEqualTo("System message with custom metadata - " + conversationId);

		assertThat(retrievedMessage.getMetadata()).containsAllEntriesOf(customMetadata);

		assertThat(retrievedMessage.getMetadata()).containsEntry("messageType", MessageType.SYSTEM);

		assertThat(retrievedMessage.getMetadata().keySet())
			.containsExactlyInAnyOrderElementsOf(new ArrayList<>(customMetadata.keySet()) {
				{
					add("messageType");
				}
			});
	}

	@Test
	void saveAllWithEmptyListClearsConversation() {
		var conversationId = UUID.randomUUID().toString();

		UserMessage initialMessage1 = new UserMessage("Initial message 1");
		AssistantMessage initialMessage2 = new AssistantMessage("Initial response 1");
		chatMemoryRepository.saveAll(conversationId, List.of(initialMessage1, initialMessage2));

		List<Message> messagesAfterInitialSave = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(messagesAfterInitialSave).hasSize(2);

		chatMemoryRepository.saveAll(conversationId, Collections.emptyList());

		List<Message> messagesAfterEmptySave = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(messagesAfterEmptySave).isEmpty();

		List<String> conversationIds = chatMemoryRepository.findConversationIds();
		assertThat(conversationIds).doesNotContain(conversationId);

		try (Session session = driver.session()) {
			Result result = session.run(
					"MATCH (s:%s {id: $conversationId}) RETURN s".formatted(config.getSessionLabel()),
					Map.of("conversationId", conversationId));
			assertThat(result.hasNext()).isFalse();
		}
	}

	@Test
	void saveAndFindMessagesWithEmptyContentOrMetadata() {
		var conversationId = UUID.randomUUID().toString();

		UserMessage messageWithEmptyContent = new UserMessage("");
		UserMessage messageWithEmptyMetadata = UserMessage.builder()
			.text("Content with empty metadata")
			.metadata(Collections.emptyMap())
			.build();

		List<Message> messagesToSave = List.of(messageWithEmptyContent, messageWithEmptyMetadata);
		chatMemoryRepository.saveAll(conversationId, messagesToSave);

		List<Message> retrievedMessages = chatMemoryRepository.findByConversationId(conversationId);
		assertThat(retrievedMessages).hasSize(2);

		Message retrievedEmptyContentMsg = retrievedMessages.get(0);
		assertThat(retrievedEmptyContentMsg).isInstanceOf(UserMessage.class);
		assertThat(retrievedEmptyContentMsg.getText()).isEqualTo("");
		assertThat(retrievedEmptyContentMsg.getMetadata()).containsEntry("messageType", MessageType.USER);

		assertThat(retrievedEmptyContentMsg.getMetadata().keySet()).hasSize(1);

		Message retrievedEmptyMetadataMsg = retrievedMessages.get(1);
		assertThat(retrievedEmptyMetadataMsg).isInstanceOf(UserMessage.class);
		assertThat(retrievedEmptyMetadataMsg.getText()).isEqualTo("Content with empty metadata");
		assertThat(retrievedEmptyMetadataMsg.getMetadata()).containsEntry("messageType", MessageType.USER);
		assertThat(retrievedEmptyMetadataMsg.getMetadata().keySet()).hasSize(1);

	}

	private Message createMessageByType(String content, MessageType messageType) {
		return switch (messageType) {
			case ASSISTANT -> new AssistantMessage(content);
			case USER -> new UserMessage(content);
			case SYSTEM -> new SystemMessage(content);
			case TOOL -> new ToolResponseMessage(List.of(new ToolResponse("id", "name", "responseData")));
		};
	}

	private static class Neo4jDriverFactory {

		static Driver create(String boltUrl) {
			return org.neo4j.driver.GraphDatabase.driver(boltUrl);
		}

	}

}
