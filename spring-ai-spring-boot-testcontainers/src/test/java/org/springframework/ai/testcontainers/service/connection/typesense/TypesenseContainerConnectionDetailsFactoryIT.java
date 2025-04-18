package org.springframework.ai.testcontainers.service.connection.typesense;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.typesense.TypesenseContainer;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.util.ResourceUtils;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.typesense.autoconfigure.TypesenseVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@TestPropertySource(properties = { "spring.ai.vectorstore.typesense.embeddingDimension=384",
		"spring.ai.vectorstore.typesense.initialize-schema=true",
		"spring.ai.vectorstore.typesense.collectionName=myTestCollection" })
@Testcontainers
class TypesenseContainerConnectionDetailsFactoryIT {

	@Container
	@ServiceConnection
	private static final TypesenseContainer typesense = new TypesenseContainer(TypesenseImage.DEFAULT_IMAGE);

	List<Document> documents = List.of(
			new Document(ResourceUtils.getText("classpath:/test/data/spring.ai.txt"), Map.of("spring", "great")),
			new Document(ResourceUtils.getText("classpath:/test/data/time.shelter.txt")), new Document(
					ResourceUtils.getText("classpath:/test/data/great.depression.txt"), Map.of("depression", "bad")));

	@Autowired
	private VectorStore vectorStore;

	@Test
	public void addAndSearch() {

		this.vectorStore.add(this.documents);

		List<Document> results = this.vectorStore
			.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());

		assertThat(results).hasSize(1);
		Document resultDoc = results.get(0);
		assertThat(resultDoc.getId()).isEqualTo(this.documents.get(0).getId());
		assertThat(resultDoc.getText())
			.contains("Spring AI provides abstractions that serve as the foundation for developing AI applications.");
		assertThat(resultDoc.getMetadata()).hasSize(2);
		assertThat(resultDoc.getMetadata()).containsKeys("spring", "distance");

		this.vectorStore.delete(this.documents.stream().map(doc -> doc.getId()).toList());

		results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());
		assertThat(results).hasSize(0);
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration(TypesenseVectorStoreAutoConfiguration.class)
	static class Config {

		@Bean
		public EmbeddingModel embeddingModel() {
			return new TransformersEmbeddingModel();
		}

	}

}
