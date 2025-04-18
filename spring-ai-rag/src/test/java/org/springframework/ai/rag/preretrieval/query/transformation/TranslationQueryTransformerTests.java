package org.springframework.ai.rag.preretrieval.query.transformation;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TranslationQueryTransformerTests {

	@Test
	void whenChatClientBuilderIsNullThenThrow() {
		assertThatThrownBy(() -> TranslationQueryTransformer.builder().chatClientBuilder(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("chatClientBuilder cannot be null");
	}

	@Test
	void whenQueryIsNullThenThrow() {
		QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
			.chatClientBuilder(mock(ChatClient.Builder.class))
			.targetLanguage("italian")
			.build();
		assertThatThrownBy(() -> queryTransformer.transform(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("query cannot be null");
	}

	@Test
	void whenPromptHasMissingTargetLanguagePlaceholderThenThrow() {
		PromptTemplate customPromptTemplate = new PromptTemplate("Translate {query}");
		assertThatThrownBy(() -> TranslationQueryTransformer.builder()
			.chatClientBuilder(mock(ChatClient.Builder.class))
			.targetLanguage("italian")
			.promptTemplate(customPromptTemplate)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("The following placeholders must be present in the prompt template")
			.hasMessageContaining("targetLanguage");
	}

	@Test
	void whenPromptHasMissingQueryPlaceholderThenThrow() {
		PromptTemplate customPromptTemplate = new PromptTemplate("Translate to {targetLanguage}");
		assertThatThrownBy(() -> TranslationQueryTransformer.builder()
			.chatClientBuilder(mock(ChatClient.Builder.class))
			.targetLanguage("italian")
			.promptTemplate(customPromptTemplate)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("The following placeholders must be present in the prompt template")
			.hasMessageContaining("query");
	}

}
