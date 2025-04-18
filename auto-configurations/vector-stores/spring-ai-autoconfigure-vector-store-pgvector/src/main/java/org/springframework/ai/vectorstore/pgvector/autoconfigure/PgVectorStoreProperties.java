package org.springframework.ai.vectorstore.pgvector.autoconfigure;

import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(PgVectorStoreProperties.CONFIG_PREFIX)
public class PgVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.pgvector";

	private int dimensions = PgVectorStore.INVALID_EMBEDDING_DIMENSION;

	private PgIndexType indexType = PgIndexType.HNSW;

	private PgDistanceType distanceType = PgDistanceType.COSINE_DISTANCE;

	private boolean removeExistingVectorStoreTable = false;

	private String tableName = PgVectorStore.DEFAULT_TABLE_NAME;

	private String schemaName = PgVectorStore.DEFAULT_SCHEMA_NAME;

	private PgVectorStore.PgIdType idType = PgVectorStore.PgIdType.UUID;

	private boolean schemaValidation = PgVectorStore.DEFAULT_SCHEMA_VALIDATION;

	private int maxDocumentBatchSize = PgVectorStore.MAX_DOCUMENT_BATCH_SIZE;

	public int getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	public PgIndexType getIndexType() {
		return this.indexType;
	}

	public void setIndexType(PgIndexType createIndexMethod) {
		this.indexType = createIndexMethod;
	}

	public PgDistanceType getDistanceType() {
		return this.distanceType;
	}

	public void setDistanceType(PgDistanceType distanceType) {
		this.distanceType = distanceType;
	}

	public boolean isRemoveExistingVectorStoreTable() {
		return this.removeExistingVectorStoreTable;
	}

	public void setRemoveExistingVectorStoreTable(boolean removeExistingVectorStoreTable) {
		this.removeExistingVectorStoreTable = removeExistingVectorStoreTable;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String vectorTableName) {
		this.tableName = vectorTableName;
	}

	public String getSchemaName() {
		return this.schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public PgVectorStore.PgIdType getIdType() {
		return this.idType;
	}

	public void setIdType(PgVectorStore.PgIdType idType) {
		this.idType = idType;
	}

	public boolean isSchemaValidation() {
		return this.schemaValidation;
	}

	public void setSchemaValidation(boolean schemaValidation) {
		this.schemaValidation = schemaValidation;
	}

	public int getMaxDocumentBatchSize() {
		return this.maxDocumentBatchSize;
	}

	public void setMaxDocumentBatchSize(int maxDocumentBatchSize) {
		this.maxDocumentBatchSize = maxDocumentBatchSize;
	}

}
