package org.springframework.ai.observation.conventions;

public enum VectorStoreObservationAttributes {

// @formatter:off

	DB_COLLECTION_NAME("db.collection.name"),

	DB_NAMESPACE("db.namespace"),

	DB_OPERATION_NAME("db.operation.name"),

	DB_RECORD_ID("db.record.id"),

	DB_SYSTEM("db.system"),

	DB_SEARCH_SIMILARITY_METRIC("db.search.similarity_metric"),

	DB_VECTOR_DIMENSION_COUNT("db.vector.dimension_count"),

	DB_VECTOR_FIELD_NAME("db.vector.field_name"),

	DB_VECTOR_QUERY_CONTENT("db.vector.query.content"),

	DB_VECTOR_QUERY_FILTER("db.vector.query.filter"),

	DB_VECTOR_QUERY_RESPONSE_DOCUMENTS("db.vector.query.response.documents"),

	DB_VECTOR_QUERY_SIMILARITY_THRESHOLD("db.vector.query.similarity_threshold"),

	DB_VECTOR_QUERY_TOP_K("db.vector.query.top_k");

	private final String value;

	VectorStoreObservationAttributes(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
