package org.springframework.ai.vectorstore.chroma.autoconfigure;

import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ChromaVectorStoreProperties.CONFIG_PREFIX)
public class ChromaVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.chroma";

	private String collectionName = ChromaVectorStore.DEFAULT_COLLECTION_NAME;

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

}
