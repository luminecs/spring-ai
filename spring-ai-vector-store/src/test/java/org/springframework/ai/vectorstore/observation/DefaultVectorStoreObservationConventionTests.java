package org.springframework.ai.vectorstore.observation;

import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.observation.conventions.SpringAiKind;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationDocumentation.LowCardinalityKeyNames;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultVectorStoreObservationConventionTests {

	private final DefaultVectorStoreObservationConvention observationConvention = new DefaultVectorStoreObservationConvention();

	@Test
	void shouldHaveName() {
		assertThat(this.observationConvention.getName())
			.isEqualTo(DefaultVectorStoreObservationConvention.DEFAULT_NAME);
	}

	@Test
	void shouldHaveContextualName() {
		VectorStoreObservationContext observationContext = VectorStoreObservationContext
			.builder("my-database", VectorStoreObservationContext.Operation.QUERY)
			.build();
		assertThat(this.observationConvention.getContextualName(observationContext)).isEqualTo("my-database query");
	}

	@Test
	void supportsOnlyVectorStoreObservationContext() {
		VectorStoreObservationContext observationContext = VectorStoreObservationContext
			.builder("my-database", VectorStoreObservationContext.Operation.QUERY)
			.build();
		assertThat(this.observationConvention.supportsContext(observationContext)).isTrue();
		assertThat(this.observationConvention.supportsContext(new Observation.Context())).isFalse();
	}

	@Test
	void shouldHaveRequiredKeyValues() {
		VectorStoreObservationContext observationContext = VectorStoreObservationContext
			.builder("my_database", VectorStoreObservationContext.Operation.QUERY)
			.build();
		assertThat(this.observationConvention.getLowCardinalityKeyValues(observationContext)).contains(
				KeyValue.of(LowCardinalityKeyNames.SPRING_AI_KIND.asString(), SpringAiKind.VECTOR_STORE.value()),
				KeyValue.of(LowCardinalityKeyNames.DB_OPERATION_NAME.asString(), "query"),
				KeyValue.of(LowCardinalityKeyNames.DB_SYSTEM.asString(), "my_database"));
	}

	@Test
	void shouldHaveOptionalKeyValues() {
		VectorStoreObservationContext observationContext = VectorStoreObservationContext
			.builder("my-database", VectorStoreObservationContext.Operation.QUERY)
			.collectionName("COLLECTION_NAME")
			.dimensions(696)
			.fieldName("FIELD_NAME")
			.namespace("NAMESPACE")
			.similarityMetric("SIMILARITY_METRIC")
			.queryRequest(SearchRequest.builder()
				.query("VDB QUERY")
				.filterExpression("country == 'UK' && year >= 2020")
				.build())
			.build();

		List<Document> queryResponseDocs = List.of(new Document("doc1"), new Document("doc2"));

		observationContext.setQueryResponse(queryResponseDocs);

		assertThat(this.observationConvention.getLowCardinalityKeyValues(observationContext))
			.contains(KeyValue.of(LowCardinalityKeyNames.DB_OPERATION_NAME.asString(),
					VectorStoreObservationContext.Operation.QUERY.value));

		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext))
			.doesNotContain(KeyValue.of(HighCardinalityKeyNames.DB_VECTOR_QUERY_RESPONSE_DOCUMENTS, "[doc1,doc2]"));

		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext)).contains(
				KeyValue.of(HighCardinalityKeyNames.DB_COLLECTION_NAME.asString(), "COLLECTION_NAME"),
				KeyValue.of(HighCardinalityKeyNames.DB_VECTOR_DIMENSION_COUNT.asString(), "696"),
				KeyValue.of(HighCardinalityKeyNames.DB_VECTOR_FIELD_NAME.asString(), "FIELD_NAME"),
				KeyValue.of(HighCardinalityKeyNames.DB_NAMESPACE.asString(), "NAMESPACE"),
				KeyValue.of(HighCardinalityKeyNames.DB_SEARCH_SIMILARITY_METRIC.asString(), "SIMILARITY_METRIC"),
				KeyValue.of(HighCardinalityKeyNames.DB_VECTOR_QUERY_CONTENT.asString(), "VDB QUERY"),
				KeyValue.of(HighCardinalityKeyNames.DB_VECTOR_QUERY_FILTER.asString(),
						"Expression[type=AND, left=Expression[type=EQ, left=Key[key=country], right=Value[value=UK]], right=Expression[type=GTE, left=Key[key=year], right=Value[value=2020]]]"));
	}

	@Test
	void shouldNotHaveKeyValuesWhenMissing() {
		VectorStoreObservationContext observationContext = VectorStoreObservationContext
			.builder("my-database", VectorStoreObservationContext.Operation.QUERY)
			.build();

		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext)
			.stream()
			.map(KeyValue::getKey)
			.toList()).doesNotContain(HighCardinalityKeyNames.DB_COLLECTION_NAME.asString(),
					HighCardinalityKeyNames.DB_VECTOR_DIMENSION_COUNT.asString(),
					HighCardinalityKeyNames.DB_VECTOR_FIELD_NAME.asString(),
					HighCardinalityKeyNames.DB_NAMESPACE.asString(),
					HighCardinalityKeyNames.DB_SEARCH_SIMILARITY_METRIC.asString(),
					HighCardinalityKeyNames.DB_VECTOR_QUERY_CONTENT.asString(),
					HighCardinalityKeyNames.DB_VECTOR_QUERY_FILTER.asString());
	}

}
