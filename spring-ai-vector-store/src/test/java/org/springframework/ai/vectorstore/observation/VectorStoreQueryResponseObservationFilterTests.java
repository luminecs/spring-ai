package org.springframework.ai.vectorstore.observation;

import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationDocumentation.HighCardinalityKeyNames;

import static org.assertj.core.api.Assertions.assertThat;

class VectorStoreQueryResponseObservationFilterTests {

	private final VectorStoreQueryResponseObservationFilter observationFilter = new VectorStoreQueryResponseObservationFilter();

	@Test
	void whenNotSupportedObservationContextThenReturnOriginalContext() {
		var expectedContext = new Observation.Context();
		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenEmptyQueryResponseThenReturnOriginalContext() {
		var expectedContext = VectorStoreObservationContext.builder("db", VectorStoreObservationContext.Operation.ADD)
			.build();

		var actualContext = this.observationFilter.map(expectedContext);

		assertThat(actualContext).isEqualTo(expectedContext);
	}

	@Test
	void whenNonEmptyQueryResponseThenAugmentContext() {
		var expectedContext = VectorStoreObservationContext.builder("db", VectorStoreObservationContext.Operation.ADD)
			.build();

		List<Document> queryResponseDocs = List.of(new Document("doc1"), new Document("doc2"));

		expectedContext.setQueryResponse(queryResponseDocs);

		var augmentedContext = this.observationFilter.map(expectedContext);

		assertThat(augmentedContext.getHighCardinalityKeyValues()).contains(KeyValue
			.of(HighCardinalityKeyNames.DB_VECTOR_QUERY_RESPONSE_DOCUMENTS.asString(), "[\"doc1\", \"doc2\"]"));
	}

}
