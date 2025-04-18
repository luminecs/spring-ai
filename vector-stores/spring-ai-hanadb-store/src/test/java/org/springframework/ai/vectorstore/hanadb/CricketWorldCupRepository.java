package org.springframework.ai.vectorstore.hanadb;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Repository;

@Repository
public class CricketWorldCupRepository implements HanaVectorRepository<CricketWorldCup> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void save(String tableName, String id, String embedding, String content) {
		String sql = String.format("""
				INSERT INTO %s (_ID, EMBEDDING, CONTENT)
				VALUES(:_id, TO_REAL_VECTOR(:embedding), :content)
				""", tableName);

		this.entityManager.createNativeQuery(sql)
			.setParameter("_id", id)
			.setParameter("embedding", embedding)
			.setParameter("content", content)
			.executeUpdate();
	}

	@Override
	@Transactional
	public int deleteEmbeddingsById(String tableName, List<String> idList) {
		String sql = String.format("""
				DELETE FROM %s WHERE _ID IN (:ids)
				""", tableName);

		return this.entityManager.createNativeQuery(sql).setParameter("ids", idList).executeUpdate();
	}

	@Override
	@Transactional
	public int deleteAllEmbeddings(String tableName) {
		String sql = String.format("""
				DELETE FROM %s
				""", tableName);

		return this.entityManager.createNativeQuery(sql).executeUpdate();
	}

	@Override
	public List<CricketWorldCup> cosineSimilaritySearch(String tableName, int topK, String queryEmbedding) {
		String sql = String.format("""
				SELECT TOP :topK * FROM %s
				ORDER BY COSINE_SIMILARITY(EMBEDDING, TO_REAL_VECTOR(:queryEmbedding)) DESC
				""", tableName);

		return this.entityManager.createNativeQuery(sql, CricketWorldCup.class)
			.setParameter("topK", topK)
			.setParameter("queryEmbedding", queryEmbedding)
			.getResultList();
	}

}
