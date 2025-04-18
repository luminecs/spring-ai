package org.springframework.ai.reader;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JsonReaderTests {

	@Value("classpath:person.json")
	private Resource ObjectResource;

	@Value("classpath:bikes.json")
	private Resource arrayResource;

	@Value("classpath:events.json")
	private Resource eventsResource;

	@Test
	void loadJsonArray() {
		assertThat(this.arrayResource).isNotNull();
		JsonReader jsonReader = new JsonReader(this.arrayResource, "description");
		List<Document> documents = jsonReader.get();
		assertThat(documents).isNotEmpty();
		for (Document document : documents) {
			assertThat(document.getText()).isNotEmpty();
		}
	}

	@Test
	void loadJsonObject() {
		assertThat(this.ObjectResource).isNotNull();
		JsonReader jsonReader = new JsonReader(this.ObjectResource, "description");
		List<Document> documents = jsonReader.get();
		assertThat(documents).isNotEmpty();
		for (Document document : documents) {
			assertThat(document.getText()).isNotEmpty();
		}
	}

	@Test
	void loadJsonArrayFromPointer() {
		assertThat(this.arrayResource).isNotNull();
		JsonReader jsonReader = new JsonReader(this.eventsResource, "description");
		List<Document> documents = jsonReader.get("/0/sessions");
		assertThat(documents).isNotEmpty();
		for (Document document : documents) {
			assertThat(document.getText()).isNotEmpty();
			assertThat(document.getText()).contains("Session");
		}
	}

	@Test
	void loadJsonObjectFromPointer() {
		assertThat(this.ObjectResource).isNotNull();
		JsonReader jsonReader = new JsonReader(this.ObjectResource, "name");
		List<Document> documents = jsonReader.get("/store");
		assertThat(documents).isNotEmpty();
		assertThat(documents.size()).isEqualTo(1);
		assertThat(documents.get(0).getText()).contains("name: Bike Shop");
	}

}
