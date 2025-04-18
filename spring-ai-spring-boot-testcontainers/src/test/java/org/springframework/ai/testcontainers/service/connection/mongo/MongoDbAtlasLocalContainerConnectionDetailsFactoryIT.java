package org.springframework.ai.testcontainers.service.connection.mongo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.autoconfigure.MongoDBAtlasVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@TestPropertySource(properties = { "spring.data.mongodb.database=simpleaidb",
		"spring.ai.vectorstore.mongodb.initialize-schema=true",
		"spring.ai.vectorstore.mongodb.collection-name=test_collection",
		"spring.ai.vectorstore.mongodb.index-name=text_index" })
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class MongoDbAtlasLocalContainerConnectionDetailsFactoryIT {

	@Container
	@ServiceConnection
	private static MongoDBAtlasLocalContainer container = new MongoDBAtlasLocalContainer(MongoDbImage.DEFAULT_IMAGE);

	@Autowired
	private VectorStore vectorStore;

	@Test
	public void addAndSearch() throws InterruptedException {
		List<Document> documents = List.of(
				new Document(
						"Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!",
						Collections.singletonMap("meta1", "meta1")),
				new Document("Hello World Hello World Hello World Hello World Hello World Hello World Hello World"),
				new Document(
						"Great Depression Great Depression Great Depression Great Depression Great Depression Great Depression",
						Collections.singletonMap("meta2", "meta2")));

		this.vectorStore.add(documents);
		Thread.sleep(5000);

		List<Document> results = this.vectorStore
			.similaritySearch(SearchRequest.builder().query("Great").topK(1).build());

		assertThat(results).hasSize(1);
		Document resultDoc = results.get(0);
		assertThat(resultDoc.getId()).isEqualTo(documents.get(2).getId());
		assertThat(resultDoc.getText()).isEqualTo(
				"Great Depression Great Depression Great Depression Great Depression Great Depression Great Depression");
		assertThat(resultDoc.getMetadata()).containsEntry("meta2", "meta2");

		this.vectorStore.delete(documents.stream().map(Document::getId).collect(Collectors.toList()));

		List<Document> results2 = this.vectorStore
			.similaritySearch(SearchRequest.builder().query("Great").topK(1).build());
		assertThat(results2).isEmpty();
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
			MongoDBAtlasVectorStoreAutoConfiguration.class })
	static class Config {

		@Bean
		public EmbeddingModel embeddingModel() {
			return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(System.getenv("OPENAI_API_KEY")).build());
		}

	}

}
