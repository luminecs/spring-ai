package org.springframework.ai.integration.tests.rag.preretrieval.query.transformation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.integration.tests.TestApplication;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
class TranslationQueryTransformerIT {

	@Autowired
	OpenAiChatModel openAiChatModel;

	@Test
	void whenTransformerWithDefaults() {
		Query query = new Query("Hvad er Danmarks hovedstad?");
		QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
			.chatClientBuilder(ChatClient.builder(this.openAiChatModel))
			.targetLanguage("english")
			.build();

		Query transformedQuery = queryTransformer.apply(query);

		assertThat(transformedQuery).isNotNull();
		System.out.println(transformedQuery);
		assertThat(transformedQuery.text()).containsIgnoringCase("Denmark").containsIgnoringCase("capital");
	}

}
