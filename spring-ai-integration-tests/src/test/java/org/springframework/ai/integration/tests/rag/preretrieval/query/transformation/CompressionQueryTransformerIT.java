package org.springframework.ai.integration.tests.rag.preretrieval.query.transformation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.integration.tests.TestApplication;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
class CompressionQueryTransformerIT {

	@Autowired
	OpenAiChatModel openAiChatModel;

	@Test
	void whenTransformerWithDefaults() {
		Query query = Query.builder()
			.text("And what is its second largest city?")
			.history(new UserMessage("What is the capital of Denmark?"),
					new AssistantMessage("Copenhagen is the capital of Denmark."))
			.build();

		QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
			.chatClientBuilder(ChatClient.builder(this.openAiChatModel))
			.build();

		Query transformedQuery = queryTransformer.apply(query);

		assertThat(transformedQuery).isNotNull();
		System.out.println(transformedQuery);
		assertThat(transformedQuery.text()).containsIgnoringCase("Denmark");
	}

}
