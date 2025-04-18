package org.springframework.ai.openai.vectorstore;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class SimplePersistentVectorStoreIT {

	@TempDir(cleanup = CleanupMode.ON_SUCCESS)
	Path workingDir;

	@Value("file:src/test/resources/data/acme/bikes.json")
	private Resource bikesJsonResource;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Test
	void persist() {
		JsonReader jsonReader = new JsonReader(this.bikesJsonResource, new ProductMetadataGenerator(), "price", "name",
				"shortDescription", "description", "tags");
		List<Document> documents = jsonReader.get();
		SimpleVectorStore vectorStore = SimpleVectorStore.builder(this.embeddingModel).build();
		vectorStore.add(documents);

		File tempFile = new File(this.workingDir.toFile(), "temp.txt");
		vectorStore.save(tempFile);
		assertThat(tempFile).isNotEmpty();
		assertThat(tempFile).content().contains("Velo 99 XR1 AXS");
		SimpleVectorStore vectorStore2 = SimpleVectorStore.builder(this.embeddingModel).build();

		vectorStore2.load(tempFile);
		List<Document> similaritySearch = vectorStore2.similaritySearch("Velo 99 XR1 AXS");
		assertThat(similaritySearch).isNotEmpty();
		assertThat(similaritySearch.get(0).getMetadata()).containsEntry("name", "Velo 99 XR1 AXS");

	}

	public class ProductMetadataGenerator implements JsonMetadataGenerator {

		@Override
		public Map<String, Object> generate(Map<String, Object> jsonMap) {
			return Map.of("name", jsonMap.get("name"));
		}

	}

}
