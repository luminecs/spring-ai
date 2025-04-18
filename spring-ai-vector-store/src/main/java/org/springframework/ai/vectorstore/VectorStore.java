package org.springframework.ai.vectorstore;

import java.util.List;
import java.util.Optional;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public interface VectorStore extends DocumentWriter {

	default String getName() {
		return this.getClass().getSimpleName();
	}

	void add(List<Document> documents);

	@Override
	default void accept(List<Document> documents) {
		add(documents);
	}

	void delete(List<String> idList);

	void delete(Filter.Expression filterExpression);

	default void delete(String filterExpression) {
		SearchRequest searchRequest = SearchRequest.builder().filterExpression(filterExpression).build();
		Filter.Expression textExpression = searchRequest.getFilterExpression();
		Assert.notNull(textExpression, "Filter expression must not be null");
		this.delete(textExpression);
	}

	@Nullable
	List<Document> similaritySearch(SearchRequest request);

	@Nullable
	default List<Document> similaritySearch(String query) {
		return this.similaritySearch(SearchRequest.builder().query(query).build());
	}

	default <T> Optional<T> getNativeClient() {
		return Optional.empty();
	}

	interface Builder<T extends Builder<T>> {

		T observationRegistry(ObservationRegistry observationRegistry);

		T customObservationConvention(VectorStoreObservationConvention convention);

		T batchingStrategy(BatchingStrategy batchingStrategy);

		VectorStore build();

	}

}
