package org.springframework.ai.observation.conventions;

public enum VectorStoreProvider {

	// @formatter:off

	AZURE("azure"),

	CASSANDRA("cassandra"),

	CHROMA("chroma"),

	COSMOSDB("cosmosdb"),

	COUCHBASE("couchbase"),

	ELASTICSEARCH("elasticsearch"),

	GEMFIRE("gemfire"),

	HANA("hana"),

	MARIADB("mariadb"),

	MILVUS("milvus"),

	MONGODB("mongodb"),

	NEO4J("neo4j"),

	OPENSEARCH("opensearch"),

	ORACLE("oracle"),

	PG_VECTOR("pg_vector"),

	PINECONE("pinecone"),

	QDRANT("qdrant"),

	REDIS("redis"),

	SIMPLE("simple"),

	TYPESENSE("typesense"),

	WEAVIATE("weaviate");

	// @formatter:on

	private final String value;

	VectorStoreProvider(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}
