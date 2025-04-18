package org.springframework.ai.vectorstore.mongodb.atlas;

import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorSearchAggregationTest {

	@Test
	void toDocumentNoFilter() {
		var vectorSearchAggregation = new VectorSearchAggregation(List.of(1.0f, 2.0f, 3.0f), "embedding", 10,
				"vector_store", 10, "");
		var aggregation = Aggregation.newAggregation(vectorSearchAggregation);
		var document = aggregation.toDocument("vector_store", Aggregation.DEFAULT_CONTEXT);

		var vectorSearchDocument = new Document("$vectorSearch",
				new Document("queryVector", List.of(1.0f, 2.0f, 3.0f)).append("path", "embedding")
					.append("numCandidates", 10)
					.append("index", "vector_store")
					.append("limit", 10));
		var expected = new Document().append("aggregate", "vector_store")
			.append("pipeline", List.of(vectorSearchDocument));
		assertEquals(expected, document);
	}

	@Test
	void toDocumentWithFilter() {
		var vectorSearchAggregation = new VectorSearchAggregation(List.of(1.0f, 2.0f, 3.0f), "embedding", 10,
				"vector_store", 10, "{\"metadata.country\":{$eq:\"BG\"}}");
		var aggregation = Aggregation.newAggregation(vectorSearchAggregation);
		var document = aggregation.toDocument("vector_store", Aggregation.DEFAULT_CONTEXT);

		var vectorSearchDocument = new Document("$vectorSearch",
				new Document("queryVector", List.of(1.0f, 2.0f, 3.0f)).append("path", "embedding")
					.append("numCandidates", 10)
					.append("index", "vector_store")
					.append("filter", new Document("metadata.country", new Document().append("$eq", "BG")))
					.append("limit", 10));
		var expected = new Document().append("aggregate", "vector_store")
			.append("pipeline", List.of(vectorSearchDocument));
		assertEquals(expected, document);
	}

}
