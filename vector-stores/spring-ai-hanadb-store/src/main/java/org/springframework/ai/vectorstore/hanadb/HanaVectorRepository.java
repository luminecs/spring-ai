package org.springframework.ai.vectorstore.hanadb;

import java.util.List;

public interface HanaVectorRepository<T extends HanaVectorEntity> {

	void save(String tableName, String id, String embedding, String content);

	int deleteEmbeddingsById(String tableName, List<String> idList);

	int deleteAllEmbeddings(String tableName);

	List<T> cosineSimilaritySearch(String tableName, int topK, String queryEmbedding);

}
