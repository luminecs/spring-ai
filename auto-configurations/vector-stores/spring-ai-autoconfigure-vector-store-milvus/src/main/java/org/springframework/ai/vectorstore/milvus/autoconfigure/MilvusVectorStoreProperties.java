package org.springframework.ai.vectorstore.milvus.autoconfigure;

import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties(MilvusVectorStoreProperties.CONFIG_PREFIX)
public class MilvusVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.milvus";

	private String databaseName = MilvusVectorStore.DEFAULT_DATABASE_NAME;

	private String collectionName = MilvusVectorStore.DEFAULT_COLLECTION_NAME;

	private int embeddingDimension = MilvusVectorStore.OPENAI_EMBEDDING_DIMENSION_SIZE;

	private MilvusIndexType indexType = MilvusIndexType.IVF_FLAT;

	private MilvusMetricType metricType = MilvusMetricType.COSINE;

	private String indexParameters = "{\"nlist\":1024}";

	private String idFieldName = MilvusVectorStore.DOC_ID_FIELD_NAME;

	private boolean isAutoId = false;

	private String contentFieldName = MilvusVectorStore.CONTENT_FIELD_NAME;

	private String metadataFieldName = MilvusVectorStore.METADATA_FIELD_NAME;

	private String embeddingFieldName = MilvusVectorStore.EMBEDDING_FIELD_NAME;

	public String getDatabaseName() {
		return this.databaseName;
	}

	public void setDatabaseName(String databaseName) {
		Assert.hasText(databaseName, "Database name should not be empty.");
		this.databaseName = databaseName;
	}

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		Assert.hasText(collectionName, "Collection name should not be empty.");
		this.collectionName = collectionName;
	}

	public int getEmbeddingDimension() {
		return this.embeddingDimension;
	}

	public void setEmbeddingDimension(int embeddingDimension) {
		Assert.isTrue(embeddingDimension > 0, "Embedding dimension should be a positive value.");
		this.embeddingDimension = embeddingDimension;
	}

	public MilvusIndexType getIndexType() {
		return this.indexType;
	}

	public void setIndexType(MilvusIndexType indexType) {
		Assert.notNull(indexType, "Index type can not be null");
		this.indexType = indexType;
	}

	public MilvusMetricType getMetricType() {
		return this.metricType;
	}

	public void setMetricType(MilvusMetricType metricType) {
		Assert.notNull(metricType, "MetricType can not be null");
		this.metricType = metricType;
	}

	public String getIndexParameters() {
		return this.indexParameters;
	}

	public void setIndexParameters(String indexParameters) {
		Assert.notNull(indexParameters, "indexParameters can not be null");
		this.indexParameters = indexParameters;
	}

	public String getIdFieldName() {
		return this.idFieldName;
	}

	public void setIdFieldName(String idFieldName) {
		Assert.notNull(idFieldName, "idFieldName can not be null");
		this.idFieldName = idFieldName;
	}

	public boolean isAutoId() {
		return this.isAutoId;
	}

	public void setAutoId(boolean autoId) {
		this.isAutoId = autoId;
	}

	public String getContentFieldName() {
		return this.contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		Assert.notNull(contentFieldName, "contentFieldName can not be null");
		this.contentFieldName = contentFieldName;
	}

	public String getMetadataFieldName() {
		return this.metadataFieldName;
	}

	public void setMetadataFieldName(String metadataFieldName) {
		Assert.notNull(metadataFieldName, "metadataFieldName can not be null");
		this.metadataFieldName = metadataFieldName;
	}

	public String getEmbeddingFieldName() {
		return this.embeddingFieldName;
	}

	public void setEmbeddingFieldName(String embeddingFieldName) {
		Assert.notNull(embeddingFieldName, "embeddingFieldName can not be null");
		this.embeddingFieldName = embeddingFieldName;
	}

	public enum MilvusMetricType {

		INVALID,

		L2,

		IP,

		COSINE,

		HAMMING,

		JACCARD

	}

	public enum MilvusIndexType {

		INVALID, FLAT, IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, DISKANN, AUTOINDEX, SCANN, GPU_IVF_FLAT, GPU_IVF_PQ, BIN_FLAT,
		BIN_IVF_FLAT, TRIE, STL_SORT

	}

}
