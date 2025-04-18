package org.springframework.ai.vectorstore;

import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class SearchRequest {

	public static final double SIMILARITY_THRESHOLD_ACCEPT_ALL = 0.0;

	public static final int DEFAULT_TOP_K = 4;

	private String query = "";

	private int topK = DEFAULT_TOP_K;

	private double similarityThreshold = SIMILARITY_THRESHOLD_ACCEPT_ALL;

	@Nullable
	private Filter.Expression filterExpression;

	public static Builder from(SearchRequest originalSearchRequest) {
		return builder().query(originalSearchRequest.getQuery())
			.topK(originalSearchRequest.getTopK())
			.similarityThreshold(originalSearchRequest.getSimilarityThreshold())
			.filterExpression(originalSearchRequest.getFilterExpression());
	}

	public SearchRequest() {
	}

	protected SearchRequest(SearchRequest original) {
		this.query = original.query;
		this.topK = original.topK;
		this.similarityThreshold = original.similarityThreshold;
		this.filterExpression = original.filterExpression;
	}

	public String getQuery() {
		return this.query;
	}

	public int getTopK() {
		return this.topK;
	}

	public double getSimilarityThreshold() {
		return this.similarityThreshold;
	}

	@Nullable
	public Filter.Expression getFilterExpression() {
		return this.filterExpression;
	}

	public boolean hasFilterExpression() {
		return this.filterExpression != null;
	}

	@Override
	public String toString() {
		return "SearchRequest{" + "query='" + this.query + '\'' + ", topK=" + this.topK + ", similarityThreshold="
				+ this.similarityThreshold + ", filterExpression=" + this.filterExpression + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SearchRequest that = (SearchRequest) o;
		return this.topK == that.topK && Double.compare(that.similarityThreshold, this.similarityThreshold) == 0
				&& Objects.equals(this.query, that.query)
				&& Objects.equals(this.filterExpression, that.filterExpression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.query, this.topK, this.similarityThreshold, this.filterExpression);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SearchRequest searchRequest = new SearchRequest();

		public Builder query(String query) {
			Assert.notNull(query, "Query can not be null.");
			this.searchRequest.query = query;
			return this;
		}

		public Builder topK(int topK) {
			Assert.isTrue(topK >= 0, "TopK should be positive.");
			this.searchRequest.topK = topK;
			return this;
		}

		public Builder similarityThreshold(double threshold) {
			Assert.isTrue(threshold >= 0 && threshold <= 1, "Similarity threshold must be in [0,1] range.");
			this.searchRequest.similarityThreshold = threshold;
			return this;
		}

		public Builder similarityThresholdAll() {
			this.searchRequest.similarityThreshold = 0.0;
			return this;
		}

		public Builder filterExpression(@Nullable Filter.Expression expression) {
			this.searchRequest.filterExpression = expression;
			return this;
		}

		public Builder filterExpression(@Nullable String textExpression) {
			this.searchRequest.filterExpression = (textExpression != null)
					? new FilterExpressionTextParser().parse(textExpression) : null;
			return this;
		}

		public SearchRequest build() {
			return this.searchRequest;
		}

	}

}
