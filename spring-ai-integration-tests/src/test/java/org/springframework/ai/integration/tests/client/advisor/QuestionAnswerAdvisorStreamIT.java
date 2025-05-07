package org.springframework.ai.integration.tests.client.advisor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.integration.tests.TestApplication;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
public class QuestionAnswerAdvisorStreamIT {

	private List<Document> knowledgeBaseDocuments;

	@Autowired
	OpenAiChatModel openAiChatModel;

	@Autowired
	PgVectorStore pgVectorStore;

	@Value("${classpath:documents/knowledge-base.md}")
	Resource knowledgeBaseResource;

	@BeforeEach
	void setUp() {
		DocumentReader markdownReader = new MarkdownDocumentReader(this.knowledgeBaseResource,
				MarkdownDocumentReaderConfig.defaultConfig());
		this.knowledgeBaseDocuments = markdownReader.read();
		this.pgVectorStore.add(this.knowledgeBaseDocuments);
	}

	@AfterEach
	void tearDown() {
		this.pgVectorStore.delete(this.knowledgeBaseDocuments.stream().map(Document::getId).toList());
	}

	@Test
	void qaStreamBasic() {
		String question = "Where does the adventure of Anacletus and Birba take place?";

		QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(this.pgVectorStore).build();

		Flux<String> responseFlux = ChatClient.builder(this.openAiChatModel)
			.build()
			.prompt(question)
			.advisors(qaAdvisor)
			.options(OpenAiChatOptions.builder().streamUsage(true).build())
			.stream()
			.content();

		String response = responseFlux.collectList().block().stream().collect(Collectors.joining());

		assertThat(response).isNotEmpty();
		assertThat(response).containsIgnoringCase("Highlands");
	}

	private record Answer(String content) {
	}

}
