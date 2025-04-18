package org.springframework.ai.rag.generation.augmentation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.util.PromptAssert;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class ContextualQueryAugmenter implements QueryAugmenter {

	private static final Logger logger = LoggerFactory.getLogger(ContextualQueryAugmenter.class);

	private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
			Context information is below.

			---------------------
			{context}
			---------------------

			Given the context information and no prior knowledge, answer the query.

			Follow these rules:

			1. If the answer is not in the context, just say that you don't know.
			2. Avoid statements like "Based on the context..." or "The provided information...".

			Query: {query}

			Answer:
			""");

	private static final PromptTemplate DEFAULT_EMPTY_CONTEXT_PROMPT_TEMPLATE = new PromptTemplate("""
			The user query is outside your knowledge base.
			Politely inform the user that you can't answer it.
			""");

	private static final boolean DEFAULT_ALLOW_EMPTY_CONTEXT = false;

	private static final Function<List<Document>, String> DEFAULT_DOCUMENT_FORMATTER = documents -> documents.stream()
		.map(Document::getText)
		.collect(Collectors.joining(System.lineSeparator()));

	private final PromptTemplate promptTemplate;

	private final PromptTemplate emptyContextPromptTemplate;

	private final boolean allowEmptyContext;

	private final Function<List<Document>, String> documentFormatter;

	public ContextualQueryAugmenter(@Nullable PromptTemplate promptTemplate,
			@Nullable PromptTemplate emptyContextPromptTemplate, @Nullable Boolean allowEmptyContext,
			@Nullable Function<List<Document>, String> documentFormatter) {
		this.promptTemplate = promptTemplate != null ? promptTemplate : DEFAULT_PROMPT_TEMPLATE;
		this.emptyContextPromptTemplate = emptyContextPromptTemplate != null ? emptyContextPromptTemplate
				: DEFAULT_EMPTY_CONTEXT_PROMPT_TEMPLATE;
		this.allowEmptyContext = allowEmptyContext != null ? allowEmptyContext : DEFAULT_ALLOW_EMPTY_CONTEXT;
		this.documentFormatter = documentFormatter != null ? documentFormatter : DEFAULT_DOCUMENT_FORMATTER;
		PromptAssert.templateHasRequiredPlaceholders(this.promptTemplate, "query", "context");
	}

	@Override
	public Query augment(Query query, List<Document> documents) {
		Assert.notNull(query, "query cannot be null");
		Assert.notNull(documents, "documents cannot be null");

		logger.debug("Augmenting query with contextual data");

		if (documents.isEmpty()) {
			return augmentQueryWhenEmptyContext(query);
		}

		String documentContext = this.documentFormatter.apply(documents);

		Map<String, Object> promptParameters = Map.of("query", query.text(), "context", documentContext);

		return new Query(this.promptTemplate.render(promptParameters));
	}

	private Query augmentQueryWhenEmptyContext(Query query) {
		if (this.allowEmptyContext) {
			logger.debug("Empty context is allowed. Returning the original query.");
			return query;
		}
		logger.debug("Empty context is not allowed. Returning a specific query for empty context.");
		return new Query(this.emptyContextPromptTemplate.render());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private PromptTemplate promptTemplate;

		private PromptTemplate emptyContextPromptTemplate;

		private Boolean allowEmptyContext;

		private Function<List<Document>, String> documentFormatter;

		public Builder promptTemplate(PromptTemplate promptTemplate) {
			this.promptTemplate = promptTemplate;
			return this;
		}

		public Builder emptyContextPromptTemplate(PromptTemplate emptyContextPromptTemplate) {
			this.emptyContextPromptTemplate = emptyContextPromptTemplate;
			return this;
		}

		public Builder allowEmptyContext(Boolean allowEmptyContext) {
			this.allowEmptyContext = allowEmptyContext;
			return this;
		}

		public Builder documentFormatter(Function<List<Document>, String> documentFormatter) {
			this.documentFormatter = documentFormatter;
			return this;
		}

		public ContextualQueryAugmenter build() {
			return new ContextualQueryAugmenter(this.promptTemplate, this.emptyContextPromptTemplate,
					this.allowEmptyContext, this.documentFormatter);
		}

	}

}
