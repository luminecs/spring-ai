package org.springframework.ai.vectorstore.mongodb.atlas;

import java.util.List;

import org.bson.Document;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.lang.NonNull;

record VectorSearchAggregation(List<Float> embeddings, String path, int numCandidates, String index, int count,
		String filter) implements AggregationOperation {

	@SuppressWarnings("null")
	@Override
	public org.bson.Document toDocument(@NonNull AggregationOperationContext context) {
		var vectorSearch = new Document("queryVector", this.embeddings).append("path", this.path)
			.append("numCandidates", this.numCandidates)
			.append("index", this.index)
			.append("limit", this.count);
		if (!this.filter.isEmpty()) {
			vectorSearch.append("filter", Document.parse(this.filter));
		}
		var doc = new Document("$vectorSearch", vectorSearch);

		return context.getMappedObject(doc);
	}

}
