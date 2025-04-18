package org.springframework.ai.chat.client.advisor.vectorstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponseStreamUtils;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class QuestionAnswerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";

	public static final String FILTER_EXPRESSION = "qa_filter_expression";

	private static final String DEFAULT_USER_TEXT_ADVISE = """

			Context information is below, surrounded by ---------------------

			---------------------
			{question_answer_context}
			---------------------

			Given the context and provided history information and not prior knowledge,
			reply to the user comment. If the answer is not in the context, inform
			the user that you can't answer the question.
			""";

	private static final int DEFAULT_ORDER = 0;

	private final VectorStore vectorStore;

	private final String userTextAdvise;

	private final SearchRequest searchRequest;

	private final boolean protectFromBlocking;

	private final int order;

	public QuestionAnswerAdvisor(VectorStore vectorStore) {
		this(vectorStore, SearchRequest.builder().build(), DEFAULT_USER_TEXT_ADVISE);
	}

	public QuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
		this(vectorStore, searchRequest, DEFAULT_USER_TEXT_ADVISE);
	}

	public QuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise) {
		this(vectorStore, searchRequest, userTextAdvise, true);
	}

	public QuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise,
			boolean protectFromBlocking) {
		this(vectorStore, searchRequest, userTextAdvise, protectFromBlocking, DEFAULT_ORDER);
	}

	public QuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise,
			boolean protectFromBlocking, int order) {

		Assert.notNull(vectorStore, "The vectorStore must not be null!");
		Assert.notNull(searchRequest, "The searchRequest must not be null!");
		Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");

		this.vectorStore = vectorStore;
		this.searchRequest = searchRequest;
		this.userTextAdvise = userTextAdvise;
		this.protectFromBlocking = protectFromBlocking;
		this.order = order;
	}

	public static Builder builder(VectorStore vectorStore) {
		return new Builder(vectorStore);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

		AdvisedRequest advisedRequest2 = before(advisedRequest);

		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest2);

		return after(advisedResponse);
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

		Flux<AdvisedResponse> advisedResponses = (this.protectFromBlocking) ?
		// @formatter:off
			Mono.just(advisedRequest)
				.publishOn(Schedulers.boundedElastic())
				.map(this::before)
				.flatMapMany(request -> chain.nextAroundStream(request))
			: chain.nextAroundStream(before(advisedRequest));
		// @formatter:on

		return advisedResponses.map(ar -> {
			if (AdvisedResponseStreamUtils.onFinishReason().test(ar)) {
				ar = after(ar);
			}
			return ar;
		});
	}

	private AdvisedRequest before(AdvisedRequest request) {

		var context = new HashMap<>(request.adviseContext());

		String advisedUserText = request.userText() + System.lineSeparator() + this.userTextAdvise;

		String query = new PromptTemplate(request.userText(), request.userParams()).render();
		var searchRequestToUse = SearchRequest.from(this.searchRequest)
			.query(query)
			.filterExpression(doGetFilterExpression(context))
			.build();

		List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);

		context.put(RETRIEVED_DOCUMENTS, documents);

		String documentContext = documents.stream()
			.map(Document::getText)
			.collect(Collectors.joining(System.lineSeparator()));

		Map<String, Object> advisedUserParams = new HashMap<>(request.userParams());
		advisedUserParams.put("question_answer_context", documentContext);

		AdvisedRequest advisedRequest = AdvisedRequest.from(request)
			.userText(advisedUserText)
			.userParams(advisedUserParams)
			.adviseContext(context)
			.build();

		return advisedRequest;
	}

	private AdvisedResponse after(AdvisedResponse advisedResponse) {
		ChatResponse.Builder chatResponseBuilder = ChatResponse.builder().from(advisedResponse.response());
		chatResponseBuilder.metadata(RETRIEVED_DOCUMENTS, advisedResponse.adviseContext().get(RETRIEVED_DOCUMENTS));
		return new AdvisedResponse(chatResponseBuilder.build(), advisedResponse.adviseContext());
	}

	protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {

		if (!context.containsKey(FILTER_EXPRESSION)
				|| !StringUtils.hasText(context.get(FILTER_EXPRESSION).toString())) {
			return this.searchRequest.getFilterExpression();
		}
		return new FilterExpressionTextParser().parse(context.get(FILTER_EXPRESSION).toString());

	}

	public static final class Builder {

		private final VectorStore vectorStore;

		private SearchRequest searchRequest = SearchRequest.builder().build();

		private String userTextAdvise = DEFAULT_USER_TEXT_ADVISE;

		private boolean protectFromBlocking = true;

		private int order = DEFAULT_ORDER;

		private Builder(VectorStore vectorStore) {
			Assert.notNull(vectorStore, "The vectorStore must not be null!");
			this.vectorStore = vectorStore;
		}

		public Builder searchRequest(SearchRequest searchRequest) {
			Assert.notNull(searchRequest, "The searchRequest must not be null!");
			this.searchRequest = searchRequest;
			return this;
		}

		public Builder userTextAdvise(String userTextAdvise) {
			Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");
			this.userTextAdvise = userTextAdvise;
			return this;
		}

		public Builder protectFromBlocking(boolean protectFromBlocking) {
			this.protectFromBlocking = protectFromBlocking;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public QuestionAnswerAdvisor build() {
			return new QuestionAnswerAdvisor(this.vectorStore, this.searchRequest, this.userTextAdvise,
					this.protectFromBlocking, this.order);
		}

	}

}
