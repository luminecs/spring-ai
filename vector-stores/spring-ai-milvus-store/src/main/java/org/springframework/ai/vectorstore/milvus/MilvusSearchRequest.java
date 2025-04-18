package org.springframework.ai.vectorstore.milvus;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.lang.Nullable;

public final class MilvusSearchRequest extends SearchRequest {

	@Nullable
	private final String nativeExpression;

	@Nullable
	private final String searchParamsJson;

	private MilvusSearchRequest(SearchRequest baseRequest, MilvusBuilder builder) {
		super(baseRequest);
		this.nativeExpression = builder.nativeExpression;
		this.searchParamsJson = builder.searchParamsJson;
	}

	@Nullable
	public String getNativeExpression() {
		return this.nativeExpression;
	}

	@Nullable
	public String getSearchParamsJson() {
		return this.searchParamsJson;
	}

	public static MilvusBuilder milvusBuilder() {
		return new MilvusBuilder();
	}

	public static class MilvusBuilder {

		private final SearchRequest.Builder baseBuilder = SearchRequest.builder();

		@Nullable
		private String nativeExpression;

		@Nullable
		private String searchParamsJson;

		public MilvusBuilder query(String query) {
			this.baseBuilder.query(query);
			return this;
		}

		public MilvusBuilder topK(int topK) {
			this.baseBuilder.topK(topK);
			return this;
		}

		public MilvusBuilder similarityThreshold(double threshold) {
			this.baseBuilder.similarityThreshold(threshold);
			return this;
		}

		public MilvusBuilder similarityThresholdAll() {
			this.baseBuilder.similarityThresholdAll();
			return this;
		}

		public MilvusBuilder filterExpression(String textExpression) {
			this.baseBuilder.filterExpression(textExpression);
			return this;
		}

		public MilvusBuilder filterExpression(Filter.Expression expression) {
			this.baseBuilder.filterExpression(expression);
			return this;
		}

		public MilvusBuilder nativeExpression(String nativeExpression) {
			this.nativeExpression = nativeExpression;
			return this;
		}

		public MilvusBuilder searchParamsJson(String searchParamsJson) {
			this.searchParamsJson = searchParamsJson;
			return this;
		}

		public MilvusSearchRequest build() {
			SearchRequest parentRequest = this.baseBuilder.build();
			return new MilvusSearchRequest(parentRequest, this);
		}

	}

}
