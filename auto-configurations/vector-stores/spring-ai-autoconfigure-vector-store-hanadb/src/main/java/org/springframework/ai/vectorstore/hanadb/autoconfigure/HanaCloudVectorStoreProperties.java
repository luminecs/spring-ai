package org.springframework.ai.vectorstore.hanadb.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(HanaCloudVectorStoreProperties.CONFIG_PREFIX)
public class HanaCloudVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.hanadb";

	private String tableName;

	private int topK;

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getTopK() {
		return this.topK;
	}

	public void setTopK(int topK) {
		this.topK = topK;
	}

}
