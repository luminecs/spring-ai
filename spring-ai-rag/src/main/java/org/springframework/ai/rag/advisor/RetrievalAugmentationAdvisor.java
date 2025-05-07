package org.springframework.ai.rag.advisor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import reactor.core.scheduler.Scheduler;

import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

public final class RetrievalAugmentationAdvisor implements BaseAdvisor {

	public static final String DOCUMENT_CONTEXT = "rag_document_context";

	private final List<QueryTransformer> queryTransformers;

	@Nullable
	private final QueryExpander queryExpander;

	private final DocumentRetriever documentRetriever;

	private final DocumentJoiner documentJoiner;

	private final QueryAugmenter queryAugmenter;

	private final TaskExecutor taskExecutor;

	private final Scheduler scheduler;

	private final int order;

	private RetrievalAugmentationAdvisor(@Nullable List<QueryTransformer> queryTransformers,
			@Nullable QueryExpander queryExpander, DocumentRetriever documentRetriever,
			@Nullable DocumentJoiner documentJoiner, @Nullable QueryAugmenter queryAugmenter,
			@Nullable TaskExecutor taskExecutor, @Nullable Scheduler scheduler, @Nullable Integer order) {
		Assert.notNull(documentRetriever, "documentRetriever cannot be null");
		Assert.noNullElements(queryTransformers, "queryTransformers cannot contain null elements");
		this.queryTransformers = queryTransformers != null ? queryTransformers : List.of();
		this.queryExpander = queryExpander;
		this.documentRetriever = documentRetriever;
		this.documentJoiner = documentJoiner != null ? documentJoiner : new ConcatenationDocumentJoiner();
		this.queryAugmenter = queryAugmenter != null ? queryAugmenter : ContextualQueryAugmenter.builder().build();
		this.taskExecutor = taskExecutor != null ? taskExecutor : buildDefaultTaskExecutor();
		this.scheduler = scheduler != null ? scheduler : BaseAdvisor.DEFAULT_SCHEDULER;
		this.order = order != null ? order : 0;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, @Nullable AdvisorChain advisorChain) {
		Map<String, Object> context = new HashMap<>(chatClientRequest.context());

		Query originalQuery = Query.builder()
			.text(chatClientRequest.prompt().getUserMessage().getText())
			.history(chatClientRequest.prompt().getInstructions())
			.context(context)
			.build();

		Query transformedQuery = originalQuery;
		for (var queryTransformer : this.queryTransformers) {
			transformedQuery = queryTransformer.apply(transformedQuery);
		}

		List<Query> expandedQueries = this.queryExpander != null ? this.queryExpander.expand(transformedQuery)
				: List.of(transformedQuery);

		Map<Query, List<List<Document>>> documentsForQuery = expandedQueries.stream()
			.map(query -> CompletableFuture.supplyAsync(() -> getDocumentsForQuery(query), this.taskExecutor))
			.toList()
			.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> List.of(entry.getValue())));

		List<Document> documents = this.documentJoiner.join(documentsForQuery);
		context.put(DOCUMENT_CONTEXT, documents);

		Query augmentedQuery = this.queryAugmenter.augment(originalQuery, documents);

		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedQuery.text()))
			.context(context)
			.build();
	}

	private Map.Entry<Query, List<Document>> getDocumentsForQuery(Query query) {
		List<Document> documents = this.documentRetriever.retrieve(query);
		return Map.entry(query, documents);
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, @Nullable AdvisorChain advisorChain) {
		ChatResponse.Builder chatResponseBuilder;
		if (chatClientResponse.chatResponse() == null) {
			chatResponseBuilder = ChatResponse.builder();
		}
		else {
			chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
		}
		chatResponseBuilder.metadata(DOCUMENT_CONTEXT, chatClientResponse.context().get(DOCUMENT_CONTEXT));
		return ChatClientResponse.builder()
			.chatResponse(chatResponseBuilder.build())
			.context(chatClientResponse.context())
			.build();
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	private static TaskExecutor buildDefaultTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setThreadNamePrefix("ai-advisor-");
		taskExecutor.setCorePoolSize(4);
		taskExecutor.setMaxPoolSize(16);
		taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
		taskExecutor.initialize();
		return taskExecutor;
	}

	public static final class Builder {

		private List<QueryTransformer> queryTransformers;

		private QueryExpander queryExpander;

		private DocumentRetriever documentRetriever;

		private DocumentJoiner documentJoiner;

		private QueryAugmenter queryAugmenter;

		private TaskExecutor taskExecutor;

		private Scheduler scheduler;

		private Integer order;

		private Builder() {
		}

		public Builder queryTransformers(List<QueryTransformer> queryTransformers) {
			this.queryTransformers = queryTransformers;
			return this;
		}

		public Builder queryTransformers(QueryTransformer... queryTransformers) {
			this.queryTransformers = Arrays.asList(queryTransformers);
			return this;
		}

		public Builder queryExpander(QueryExpander queryExpander) {
			this.queryExpander = queryExpander;
			return this;
		}

		public Builder documentRetriever(DocumentRetriever documentRetriever) {
			this.documentRetriever = documentRetriever;
			return this;
		}

		public Builder documentJoiner(DocumentJoiner documentJoiner) {
			this.documentJoiner = documentJoiner;
			return this;
		}

		public Builder queryAugmenter(QueryAugmenter queryAugmenter) {
			this.queryAugmenter = queryAugmenter;
			return this;
		}

		public Builder taskExecutor(TaskExecutor taskExecutor) {
			this.taskExecutor = taskExecutor;
			return this;
		}

		public Builder scheduler(Scheduler scheduler) {
			this.scheduler = scheduler;
			return this;
		}

		public Builder order(Integer order) {
			this.order = order;
			return this;
		}

		public RetrievalAugmentationAdvisor build() {
			return new RetrievalAugmentationAdvisor(this.queryTransformers, this.queryExpander, this.documentRetriever,
					this.documentJoiner, this.queryAugmenter, this.taskExecutor, this.scheduler, this.order);
		}

	}

}
