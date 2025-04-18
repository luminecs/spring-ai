package org.springframework.ai.vectorstore.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.cypherdsl.support.schema_name.SchemaNames;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.observation.conventions.VectorStoreSimilarityMetric;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.neo4j.filter.Neo4jVectorFilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class Neo4jVectorStore extends AbstractObservationVectorStore implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(Neo4jVectorStore.class);

	public static final int DEFAULT_EMBEDDING_DIMENSION = 1536;

	public static final int DEFAULT_TRANSACTION_SIZE = 10_000;

	public static final String DEFAULT_LABEL = "Document";

	public static final String DEFAULT_INDEX_NAME = "spring-ai-document-index";

	public static final String DEFAULT_EMBEDDING_PROPERTY = "embedding";

	public static final String DEFAULT_ID_PROPERTY = "id";

	public static final String DEFAULT_CONSTRAINT_NAME = DEFAULT_LABEL + "_unique_idx";

	private static final Map<Neo4jDistanceType, VectorStoreSimilarityMetric> SIMILARITY_TYPE_MAPPING = Map.of(
			Neo4jDistanceType.COSINE, VectorStoreSimilarityMetric.COSINE, Neo4jDistanceType.EUCLIDEAN,
			VectorStoreSimilarityMetric.EUCLIDEAN);

	private final Driver driver;

	private final SessionConfig sessionConfig;

	private final int embeddingDimension;

	private final Neo4jDistanceType distanceType;

	private final String embeddingProperty;

	private final String label;

	private final String indexName;

	private final String indexNameNotSanitized;

	private final String idProperty;

	private final String constraintName;

	private final Neo4jVectorFilterExpressionConverter filterExpressionConverter = new Neo4jVectorFilterExpressionConverter();

	private final boolean initializeSchema;

	protected Neo4jVectorStore(Builder builder) {
		super(builder);

		Assert.notNull(builder.driver, "Neo4j driver must not be null");

		this.driver = builder.driver;
		this.sessionConfig = builder.sessionConfig;
		this.embeddingDimension = builder.embeddingDimension;
		this.distanceType = builder.distanceType;
		this.embeddingProperty = SchemaNames.sanitize(builder.embeddingProperty).orElseThrow();
		this.label = SchemaNames.sanitize(builder.label).orElseThrow();
		this.indexNameNotSanitized = builder.indexName;
		this.indexName = SchemaNames.sanitize(builder.indexName, true).orElseThrow();
		this.idProperty = SchemaNames.sanitize(builder.idProperty).orElseThrow();
		this.constraintName = SchemaNames.sanitize(builder.constraintName).orElseThrow();
		this.initializeSchema = builder.initializeSchema;
	}

	@Override
	public void doAdd(List<Document> documents) {

		List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(),
				this.batchingStrategy);

		var rows = documents.stream()
			.map(document -> documentToRecord(document, embeddings.get(documents.indexOf(document))))
			.toList();

		try (var session = this.driver.session()) {
			var statement = """
						UNWIND $rows AS row
						MERGE (u:%s {%2$s: row.id})
							SET u += row.properties
						WITH row, u
						CALL db.create.setNodeVectorProperty(u, $embeddingProperty, row[$embeddingProperty])
					""".formatted(this.label, this.idProperty);
			session
				.executeWrite(tx -> tx.run(statement, Map.of("rows", rows, "embeddingProperty", this.embeddingProperty))
					.consume());
		}
	}

	@Override
	public void doDelete(List<String> idList) {

		try (var session = this.driver.session(this.sessionConfig)) {

			session
				.run("""
						MATCH (n:%s) WHERE n.%s IN $ids
						CALL { WITH n DETACH DELETE n } IN TRANSACTIONS OF $transactionSize ROWS
						""".formatted(this.label, this.idProperty),
						Map.of("ids", idList, "transactionSize", DEFAULT_TRANSACTION_SIZE))
				.consume();
		}
	}

	@Override
	protected void doDelete(Filter.Expression filterExpression) {
		Assert.notNull(filterExpression, "Filter expression must not be null");

		try (var session = this.driver.session(this.sessionConfig)) {
			String whereClause = this.filterExpressionConverter.convertExpression(filterExpression);

			String cypher = """
					MATCH (node:%s) WHERE %s
					CALL { WITH node DETACH DELETE node } IN TRANSACTIONS OF $transactionSize ROWS
					""".formatted(this.label, whereClause);

			var summary = session.run(cypher, Map.of("transactionSize", DEFAULT_TRANSACTION_SIZE)).consume();

			logger.debug("Deleted {} nodes matching filter expression", summary.counters().nodesDeleted());
		}
		catch (Exception e) {
			logger.error("Failed to delete nodes by filter: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to delete nodes by filter", e);
		}
	}

	@Override
	public List<Document> doSimilaritySearch(SearchRequest request) {
		Assert.isTrue(request.getTopK() > 0, "The number of documents to returned must be greater than zero");
		Assert.isTrue(request.getSimilarityThreshold() >= 0 && request.getSimilarityThreshold() <= 1,
				"The similarity score is bounded between 0 and 1; least to most similar respectively.");

		var embedding = Values.value(this.embeddingModel.embed(request.getQuery()));
		try (var session = this.driver.session(this.sessionConfig)) {
			StringBuilder condition = new StringBuilder("score >= $threshold");
			if (request.hasFilterExpression()) {
				condition.append(" AND ")
					.append(this.filterExpressionConverter.convertExpression(request.getFilterExpression()));
			}
			String query = """
					CALL db.index.vector.queryNodes($indexName, $numberOfNearestNeighbours, $embeddingValue)
					YIELD node, score
					WHERE %s
					RETURN node, score""".formatted(condition);

			return session.executeRead(tx -> tx
				.run(query,
						Map.of("indexName", this.indexNameNotSanitized, "numberOfNearestNeighbours", request.getTopK(),
								"embeddingValue", embedding, "threshold", request.getSimilarityThreshold()))
				.list(this::recordToDocument));
		}
	}

	@Override
	public void afterPropertiesSet() {

		if (!this.initializeSchema) {
			return;
		}

		try (var session = this.driver.session(this.sessionConfig)) {

			session.executeWriteWithoutResult(tx -> {
				tx.run("CREATE CONSTRAINT %s IF NOT EXISTS FOR (n:%s) REQUIRE n.%s IS UNIQUE"
					.formatted(this.constraintName, this.label, this.idProperty)).consume();

				var statement = """
						CREATE VECTOR INDEX %s IF NOT EXISTS FOR (n:%s) ON (n.%s)
								OPTIONS {indexConfig: {
								`vector.dimensions`: %d,
								`vector.similarity_function`: '%s'
								}}
						""".formatted(this.indexName, this.label, this.embeddingProperty, this.embeddingDimension,
						this.distanceType.name);
				tx.run(statement).consume();
			});

			session.run("CALL db.awaitIndexes()").consume();
		}
	}

	private Map<String, Object> documentToRecord(Document document, float[] embedding) {

		var row = new HashMap<String, Object>();

		row.put("id", document.getId());

		var properties = new HashMap<String, Object>();
		properties.put("text", document.getText());

		document.getMetadata().forEach((k, v) -> properties.put("metadata." + k, Values.value(v)));
		row.put("properties", properties);

		row.put(this.embeddingProperty, Values.value(embedding));
		return row;
	}

	private Document recordToDocument(org.neo4j.driver.Record neoRecord) {
		var node = neoRecord.get("node").asNode();
		var score = neoRecord.get("score").asFloat();
		var metaData = new HashMap<String, Object>();
		metaData.put(DocumentMetadata.DISTANCE.value(), 1 - score);
		node.keys().forEach(key -> {
			if (key.startsWith("metadata.")) {
				metaData.put(key.substring(key.indexOf(".") + 1), node.get(key).asObject());
			}
		});

		return Document.builder()
			.id(node.get(this.idProperty).asString())
			.text(node.get("text").asString())
			.metadata(Map.copyOf(metaData))
			.score((double) score)
			.build();
	}

	@Override
	public VectorStoreObservationContext.Builder createObservationContextBuilder(String operationName) {

		return VectorStoreObservationContext.builder(VectorStoreProvider.NEO4J.value(), operationName)
			.collectionName(this.indexName)
			.dimensions(this.embeddingModel.dimensions())
			.similarityMetric(getSimilarityMetric());
	}

	private String getSimilarityMetric() {
		if (!SIMILARITY_TYPE_MAPPING.containsKey(this.distanceType)) {
			return this.distanceType.name();
		}
		return SIMILARITY_TYPE_MAPPING.get(this.distanceType).value();
	}

	@Override
	public <T> Optional<T> getNativeClient() {
		@SuppressWarnings("unchecked")
		T client = (T) this.driver;
		return Optional.of(client);
	}

	public enum Neo4jDistanceType {

		COSINE("cosine"), EUCLIDEAN("euclidean");

		public final String name;

		Neo4jDistanceType(String name) {
			this.name = name;
		}

	}

	public static Builder builder(Driver driver, EmbeddingModel embeddingModel) {
		return new Builder(driver, embeddingModel);
	}

	public static class Builder extends AbstractVectorStoreBuilder<Builder> {

		private final Driver driver;

		private SessionConfig sessionConfig = SessionConfig.defaultConfig();

		private int embeddingDimension = DEFAULT_EMBEDDING_DIMENSION;

		private Neo4jDistanceType distanceType = Neo4jDistanceType.COSINE;

		private String label = DEFAULT_LABEL;

		private String embeddingProperty = DEFAULT_EMBEDDING_PROPERTY;

		private String indexName = DEFAULT_INDEX_NAME;

		private String idProperty = DEFAULT_ID_PROPERTY;

		private String constraintName = DEFAULT_CONSTRAINT_NAME;

		private boolean initializeSchema = false;

		private Builder(Driver driver, EmbeddingModel embeddingModel) {
			super(embeddingModel);
			Assert.notNull(driver, "Neo4j driver must not be null");
			this.driver = driver;
		}

		public Builder databaseName(String databaseName) {
			if (StringUtils.hasText(databaseName)) {
				this.sessionConfig = SessionConfig.forDatabase(databaseName);
			}
			return this;
		}

		public Builder sessionConfig(SessionConfig sessionConfig) {
			this.sessionConfig = sessionConfig;
			return this;
		}

		public Builder embeddingDimension(int dimension) {
			Assert.isTrue(dimension >= 1, "Dimension has to be positive");
			this.embeddingDimension = dimension;
			return this;
		}

		public Builder distanceType(Neo4jDistanceType distanceType) {
			Assert.notNull(distanceType, "Distance type may not be null");
			this.distanceType = distanceType;
			return this;
		}

		public Builder label(String label) {
			if (StringUtils.hasText(label)) {
				this.label = label;
			}
			return this;
		}

		public Builder embeddingProperty(String embeddingProperty) {
			if (StringUtils.hasText(embeddingProperty)) {
				this.embeddingProperty = embeddingProperty;
			}
			return this;
		}

		public Builder indexName(String indexName) {
			if (StringUtils.hasText(indexName)) {
				this.indexName = indexName;
			}
			return this;
		}

		public Builder idProperty(String idProperty) {
			if (StringUtils.hasText(idProperty)) {
				this.idProperty = idProperty;
			}
			return this;
		}

		public Builder constraintName(String constraintName) {
			if (StringUtils.hasText(constraintName)) {
				this.constraintName = constraintName;
			}
			return this;
		}

		public Builder initializeSchema(boolean initializeSchema) {
			this.initializeSchema = initializeSchema;
			return this;
		}

		@Override
		public Neo4jVectorStore build() {
			return new Neo4jVectorStore(this);
		}

	}

}
