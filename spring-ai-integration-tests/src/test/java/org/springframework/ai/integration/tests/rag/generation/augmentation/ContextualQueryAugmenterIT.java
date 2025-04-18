package org.springframework.ai.integration.tests.rag.generation.augmentation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.document.Document;
import org.springframework.ai.integration.tests.TestApplication;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
class ContextualQueryAugmenterIT {

	@Autowired
	OpenAiChatModel openAiChatModel;

	@Test
	void whenContextIsProvided() {
		QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().build();
		Query query = new Query("What is Iorek's dream?");
		List<Document> documents = List
			.of(new Document("Iorek was a little polar bear who lived in the Arctic circle."), new Document(
					"Iorek loved to explore the snowy landscape and dreamt of one day going on an adventure around the North Pole."));

		Query augmentedQuery = queryAugmenter.augment(query, documents);
		String response = this.openAiChatModel.call(augmentedQuery.text());

		assertThat(response).isNotEmpty();
		System.out.println(response);
		assertThat(response).containsIgnoringCase("North Pole");
		assertThat(response).doesNotContainIgnoringCase("context");
		assertThat(response).doesNotContainIgnoringCase("information");
	}

	@Test
	void whenAllowEmptyContext() {
		QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().allowEmptyContext(true).build();
		Query query = new Query("What is Iorek's dream?");
		List<Document> documents = List.of();
		Query augmentedQuery = queryAugmenter.augment(query, documents);
		String response = this.openAiChatModel.call(augmentedQuery.text());

		assertThat(response).isNotEmpty();
		System.out.println(response);
		assertThat(response).containsIgnoringCase("Iorek");
	}

	@Test
	void whenNotAllowEmptyContext() {
		QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().build();
		Query query = new Query("What is Iorek's dream?");
		List<Document> documents = List.of();
		Query augmentedQuery = queryAugmenter.augment(query, documents);
		String response = this.openAiChatModel.call(augmentedQuery.text());

		assertThat(response).isNotEmpty();
		System.out.println(response);
		assertThat(response).doesNotContainIgnoringCase("Iorek");
	}

}
