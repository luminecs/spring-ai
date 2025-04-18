package org.springframework.ai.vectorstore.hanadb.autoconfigure;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.document.Document;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HANA_DATASOURCE_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HANA_DATASOURCE_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HANA_DATASOURCE_PASSWORD", matches = ".+")
@Disabled
public class HanaCloudVectorStoreAutoConfigurationIT {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(HanaCloudVectorStoreAutoConfiguration.class,
				OpenAiEmbeddingAutoConfiguration.class, RestClientAutoConfiguration.class,
				SpringAiRetryAutoConfiguration.class, JdbcRepositoriesAutoConfiguration.class))
		.withPropertyValues("spring.ai.openai.api-key=" + System.getenv("OPENAI_API_KEY"),
				"spring.ai.openai.embedding.options.model=text-embedding-ada-002",
				"spring.datasource.url=" + System.getenv("HANA_DATASOURCE_URL"),
				"spring.datasource.username=" + System.getenv("HANA_DATASOURCE_USERNAME"),
				"spring.datasource.password=" + System.getenv("HANA_DATASOURCE_PASSWORD"),
				"spring.ai.vectorstore.hanadb.tableName=CRICKET_WORLD_CUP", "spring.ai.vectorstore.hanadb.topK=1");

	List<Document> documents = List.of(
			new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!"),
			new Document("Hello World Hello World Hello World Hello World Hello World Hello World Hello World"),
			new Document(
					"Great Depression Great Depression Great Depression Great Depression Great Depression Great Depression"));

	@Test
	public void addAndSearch() {
		this.contextRunner.run(context -> {
			VectorStore vectorStore = context.getBean(VectorStore.class);
			vectorStore.add(this.documents);

			List<Document> results = vectorStore.similaritySearch("What is Great Depression?");
			Assertions.assertEquals(1, results.size());

			vectorStore.delete(this.documents.stream().map(Document::getId).toList());
			List<Document> results2 = vectorStore.similaritySearch("Great Depression");
			Assertions.assertEquals(0, results2.size());
		});
	}

}
