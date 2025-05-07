package org.springframework.ai.model.transformer;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.util.Assert;

public class KeywordMetadataEnricher implements DocumentTransformer {

	public static final String CONTEXT_STR_PLACEHOLDER = "context_str";

	public static final String KEYWORDS_TEMPLATE = """
			{context_str}. Give %s unique keywords for this
			document. Format as comma separated. Keywords: """;

	private static final String EXCERPT_KEYWORDS_METADATA_KEY = "excerpt_keywords";

	private final ChatModel chatModel;

	private final int keywordCount;

	public KeywordMetadataEnricher(ChatModel chatModel, int keywordCount) {
		Assert.notNull(chatModel, "ChatModel must not be null");
		Assert.isTrue(keywordCount >= 1, "Document count must be >= 1");

		this.chatModel = chatModel;
		this.keywordCount = keywordCount;
	}

	@Override
	public List<Document> apply(List<Document> documents) {
		for (Document document : documents) {

			var template = new PromptTemplate(String.format(KEYWORDS_TEMPLATE, this.keywordCount));
			Prompt prompt = template.create(Map.of(CONTEXT_STR_PLACEHOLDER, document.getText()));
			String keywords = this.chatModel.call(prompt).getResult().getOutput().getText();
			document.getMetadata().putAll(Map.of(EXCERPT_KEYWORDS_METADATA_KEY, keywords));
		}
		return documents;
	}

}
