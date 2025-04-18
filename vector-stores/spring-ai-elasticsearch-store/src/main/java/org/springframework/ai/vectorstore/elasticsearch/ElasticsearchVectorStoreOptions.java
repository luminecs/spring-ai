package org.springframework.ai.vectorstore.elasticsearch;

public class ElasticsearchVectorStoreOptions {

	private String indexName = "spring-ai-document-index";

	private int dimensions = 1536;

	private SimilarityFunction similarity = SimilarityFunction.cosine;

	private String embeddingFieldName = "embedding";

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public int getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(int dims) {
		this.dimensions = dims;
	}

	public SimilarityFunction getSimilarity() {
		return this.similarity;
	}

	public void setSimilarity(SimilarityFunction similarity) {
		this.similarity = similarity;
	}

	public String getEmbeddingFieldName() {
		return this.embeddingFieldName;
	}

	public void setEmbeddingFieldName(String embeddingFieldName) {
		this.embeddingFieldName = embeddingFieldName;
	}

}
