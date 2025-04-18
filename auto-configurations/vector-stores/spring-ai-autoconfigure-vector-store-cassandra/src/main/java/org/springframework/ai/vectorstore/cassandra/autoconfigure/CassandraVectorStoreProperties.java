package org.springframework.ai.vectorstore.cassandra.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.vectorstore.cassandra.CassandraVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties(CassandraVectorStoreProperties.CONFIG_PREFIX)
public class CassandraVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.cassandra";

	private static final Logger logger = LoggerFactory.getLogger(CassandraVectorStoreProperties.class);

	private String keyspace = CassandraVectorStore.DEFAULT_KEYSPACE_NAME;

	private String table = CassandraVectorStore.DEFAULT_TABLE_NAME;

	private String indexName = null;

	private String contentColumnName = CassandraVectorStore.DEFAULT_CONTENT_COLUMN_NAME;

	private String embeddingColumnName = CassandraVectorStore.DEFAULT_EMBEDDING_COLUMN_NAME;

	private boolean returnEmbeddings = false;

	private int fixedThreadPoolExecutorSize = CassandraVectorStore.DEFAULT_ADD_CONCURRENCY;

	public String getKeyspace() {
		return this.keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public String getTable() {
		return this.table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getContentColumnName() {
		return this.contentColumnName;
	}

	public void setContentColumnName(String contentColumnName) {
		this.contentColumnName = contentColumnName;
	}

	public String getEmbeddingColumnName() {
		return this.embeddingColumnName;
	}

	public void setEmbeddingColumnName(String embeddingColumnName) {
		this.embeddingColumnName = embeddingColumnName;
	}

	public boolean getReturnEmbeddings() {
		return this.returnEmbeddings;
	}

	public void setReturnEmbeddings(boolean returnEmbeddings) {
		this.returnEmbeddings = returnEmbeddings;
	}

	public int getFixedThreadPoolExecutorSize() {
		return this.fixedThreadPoolExecutorSize;
	}

	public void setFixedThreadPoolExecutorSize(int fixedThreadPoolExecutorSize) {
		Assert.state(0 < fixedThreadPoolExecutorSize, "Thread-pool size must be greater than zero");
		this.fixedThreadPoolExecutorSize = fixedThreadPoolExecutorSize;
	}

}
