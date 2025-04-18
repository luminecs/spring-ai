package org.springframework.ai.vectorstore.oracle.autoconfigure;

import org.springframework.ai.vectorstore.oracle.OracleVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OracleVectorStoreProperties.CONFIG_PREFIX)
public class OracleVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.oracle";

	private String tableName = OracleVectorStore.DEFAULT_TABLE_NAME;

	private OracleVectorStore.OracleVectorStoreIndexType indexType = OracleVectorStore.DEFAULT_INDEX_TYPE;

	private OracleVectorStore.OracleVectorStoreDistanceType distanceType = OracleVectorStore.DEFAULT_DISTANCE_TYPE;

	private int dimensions = OracleVectorStore.DEFAULT_DIMENSIONS;

	private boolean removeExistingVectorStoreTable;

	private boolean forcedNormalization;

	private int searchAccuracy = OracleVectorStore.DEFAULT_SEARCH_ACCURACY;

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public OracleVectorStore.OracleVectorStoreIndexType getIndexType() {
		return this.indexType;
	}

	public void setIndexType(OracleVectorStore.OracleVectorStoreIndexType indexType) {
		this.indexType = indexType;
	}

	public OracleVectorStore.OracleVectorStoreDistanceType getDistanceType() {
		return this.distanceType;
	}

	public void setDistanceType(OracleVectorStore.OracleVectorStoreDistanceType distanceType) {
		this.distanceType = distanceType;
	}

	public int getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	public boolean isRemoveExistingVectorStoreTable() {
		return this.removeExistingVectorStoreTable;
	}

	public void setRemoveExistingVectorStoreTable(boolean removeExistingVectorStoreTable) {
		this.removeExistingVectorStoreTable = removeExistingVectorStoreTable;
	}

	public boolean isForcedNormalization() {
		return this.forcedNormalization;
	}

	public void setForcedNormalization(boolean forcedNormalization) {
		this.forcedNormalization = forcedNormalization;
	}

	public int getSearchAccuracy() {
		return this.searchAccuracy;
	}

	public void setSearchAccuracy(int searchAccuracy) {
		this.searchAccuracy = searchAccuracy;
	}

}
