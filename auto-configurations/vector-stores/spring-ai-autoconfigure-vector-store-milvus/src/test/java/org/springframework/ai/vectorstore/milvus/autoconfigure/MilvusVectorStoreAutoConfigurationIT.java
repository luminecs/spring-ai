package org.springframework.ai.vectorstore.milvus.autoconfigure;

import java.util.List;
import java.util.Map;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.milvus.MilvusContainer;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.test.vectorstore.ObservationTestUtil;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.util.ResourceUtils;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class MilvusVectorStoreAutoConfigurationIT {

	@Container
	private static MilvusContainer milvus = new MilvusContainer("milvusdb/milvus:v2.3.8");

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MilvusVectorStoreAutoConfiguration.class))
		.withUserConfiguration(Config.class);

	List<Document> documents = List.of(
			new Document(ResourceUtils.getText("classpath:/test/data/spring.ai.txt"), Map.of("spring", "great")),
			new Document(ResourceUtils.getText("classpath:/test/data/time.shelter.txt")), new Document(
					ResourceUtils.getText("classpath:/test/data/great.depression.txt"), Map.of("depression", "bad")));

	@Test
	public void addAndSearch() {
		this.contextRunner
			.withPropertyValues("spring.ai.vectorstore.milvus.metricType=COSINE",
					"spring.ai.vectorstore.milvus.indexType=IVF_FLAT",
					"spring.ai.vectorstore.milvus.embeddingDimension=384",
					"spring.ai.vectorstore.milvus.collectionName=myTestCollection",
					"spring.ai.vectorstore.milvus.initializeSchema=true",
					"spring.ai.vectorstore.milvus.client.host=" + milvus.getHost(),
					"spring.ai.vectorstore.milvus.client.port=" + milvus.getMappedPort(19530))
			.run(context -> {
				VectorStore vectorStore = context.getBean(VectorStore.class);
				TestObservationRegistry observationRegistry = context.getBean(TestObservationRegistry.class);

				vectorStore.add(this.documents);

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.ADD);
				observationRegistry.clear();

				List<Document> results = vectorStore
					.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());

				assertThat(results).hasSize(1);
				Document resultDoc = results.get(0);
				assertThat(resultDoc.getId()).isEqualTo(this.documents.get(0).getId());
				assertThat(resultDoc.getText()).contains(
						"Spring AI provides abstractions that serve as the foundation for developing AI applications.");
				assertThat(resultDoc.getMetadata()).hasSize(2);
				assertThat(resultDoc.getMetadata()).containsKeys("spring", "distance");

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.QUERY);
				observationRegistry.clear();

				vectorStore.delete(this.documents.stream().map(doc -> doc.getId()).toList());

				results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());
				assertThat(results).hasSize(0);

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.DELETE);
				observationRegistry.clear();

			});
	}

	@Test
	public void searchWithCustomFields() {
		this.contextRunner
			.withPropertyValues("spring.ai.vectorstore.milvus.metricType=COSINE",
					"spring.ai.vectorstore.milvus.indexType=IVF_FLAT",
					"spring.ai.vectorstore.milvus.embeddingDimension=384",
					"spring.ai.vectorstore.milvus.collectionName=myCustomCollection",
					"spring.ai.vectorstore.milvus.idFieldName=identity",
					"spring.ai.vectorstore.milvus.contentFieldName=text",
					"spring.ai.vectorstore.milvus.embeddingFieldName=vectors",
					"spring.ai.vectorstore.milvus.metadataFieldName=meta",
					"spring.ai.vectorstore.milvus.initializeSchema=true",
					"spring.ai.vectorstore.milvus.client.host=" + milvus.getHost(),
					"spring.ai.vectorstore.milvus.client.port=" + milvus.getMappedPort(19530))
			.run(context -> {
				VectorStore vectorStore = context.getBean(VectorStore.class);
				TestObservationRegistry observationRegistry = context.getBean(TestObservationRegistry.class);

				vectorStore.add(this.documents);

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.ADD);
				observationRegistry.clear();

				List<Document> results = vectorStore
					.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());

				assertThat(results).hasSize(1);
				Document resultDoc = results.get(0);
				assertThat(resultDoc.getId()).isEqualTo(this.documents.get(0).getId());
				assertThat(resultDoc.getText()).contains(
						"Spring AI provides abstractions that serve as the foundation for developing AI applications.");
				assertThat(resultDoc.getMetadata()).hasSize(2);
				assertThat(resultDoc.getMetadata()).containsKeys("spring", "distance");

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.QUERY);
				observationRegistry.clear();

				vectorStore.delete(this.documents.stream().map(doc -> doc.getId()).toList());

				results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(1).build());
				assertThat(results).hasSize(0);

				ObservationTestUtil.assertObservationRegistry(observationRegistry, VectorStoreProvider.MILVUS,
						VectorStoreObservationContext.Operation.DELETE);
				observationRegistry.clear();

			});
	}

	@Test
	public void autoConfigurationDisabledWhenTypeIsNone() {
		this.contextRunner.withPropertyValues("spring.ai.vectorstore.type=none").run(context -> {
			assertThat(context.getBeansOfType(MilvusVectorStoreProperties.class)).isEmpty();
			assertThat(context.getBeansOfType(MilvusVectorStore.class)).isEmpty();
			assertThat(context.getBeansOfType(VectorStore.class)).isEmpty();
		});
	}

	@Test
	public void autoConfigurationEnabledByDefault() {
		this.contextRunner
			.withPropertyValues("spring.ai.vectorstore.milvus.client.host=" + milvus.getHost(),
					"spring.ai.vectorstore.milvus.client.port=" + milvus.getMappedPort(19530))
			.run(context -> {
				assertThat(context.getBeansOfType(MilvusVectorStoreProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(VectorStore.class)).isNotEmpty();
				assertThat(context.getBean(VectorStore.class)).isInstanceOf(MilvusVectorStore.class);
			});
	}

	@Test
	public void autoConfigurationEnabledWhenTypeIsMilvus() {
		this.contextRunner
			.withPropertyValues("spring.ai.vectorstore.milvus.client.host=" + milvus.getHost(),
					"spring.ai.vectorstore.milvus.client.port=" + milvus.getMappedPort(19530))
			.withPropertyValues("spring.ai.vectorstore.type=milvus")
			.run(context -> {
				assertThat(context.getBeansOfType(MilvusVectorStoreProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(VectorStore.class)).isNotEmpty();
				assertThat(context.getBean(VectorStore.class)).isInstanceOf(MilvusVectorStore.class);
			});
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		public TestObservationRegistry observationRegistry() {
			return TestObservationRegistry.create();
		}

		@Bean
		public EmbeddingModel embeddingModel() {
			return new TransformersEmbeddingModel();
		}

	}

}
