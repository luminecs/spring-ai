package org.springframework.ai.vectorstore.typesense.autoconfigure;

import org.springframework.ai.vectorstore.properties.CommonVectorStoreProperties;
import org.springframework.ai.vectorstore.typesense.TypesenseVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(TypesenseVectorStoreProperties.CONFIG_PREFIX)
public class TypesenseVectorStoreProperties extends CommonVectorStoreProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.typesense";

	private String collectionName = TypesenseVectorStore.DEFAULT_COLLECTION_NAME;

	private int embeddingDimension = TypesenseVectorStore.OPENAI_EMBEDDING_DIMENSION_SIZE;

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public int getEmbeddingDimension() {
		return this.embeddingDimension;
	}

	public void setEmbeddingDimension(int embeddingDimension) {
		this.embeddingDimension = embeddingDimension;
	}

}
