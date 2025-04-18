package org.springframework.ai.rag.retrieval.join;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcatenationDocumentJoinerTests {

	@Test
	void whenDocumentsForQueryIsNullThenThrow() {
		DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
		assertThatThrownBy(() -> documentJoiner.apply(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("documentsForQuery cannot be null");
	}

	@Test
	void whenDocumentsForQueryContainsNullKeysThenThrow() {
		DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
		var documentsForQuery = new HashMap<Query, List<List<Document>>>();
		documentsForQuery.put(null, List.of());
		assertThatThrownBy(() -> documentJoiner.apply(documentsForQuery)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("documentsForQuery cannot contain null keys");
	}

	@Test
	void whenDocumentsForQueryContainsNullValuesThenThrow() {
		DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
		var documentsForQuery = new HashMap<Query, List<List<Document>>>();
		documentsForQuery.put(new Query("test"), null);
		assertThatThrownBy(() -> documentJoiner.apply(documentsForQuery)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("documentsForQuery cannot contain null values");
	}

	@Test
	void whenNoDuplicatedDocumentsThenAllDocumentsAreJoined() {
		DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
		var documentsForQuery = new HashMap<Query, List<List<Document>>>();
		documentsForQuery.put(new Query("query1"),
				List.of(List.of(new Document("1", "Content 1", Map.of()), new Document("2", "Content 2", Map.of())),
						List.of(new Document("3", "Content 3", Map.of()))));
		documentsForQuery.put(new Query("query2"), List.of(List.of(new Document("4", "Content 4", Map.of()))));

		List<Document> result = documentJoiner.join(documentsForQuery);

		assertThat(result).hasSize(4);
		assertThat(result).extracting(Document::getId).containsExactlyInAnyOrder("1", "2", "3", "4");
	}

	@Test
	void whenDuplicatedDocumentsThenOnlyFirstOccurrenceIsKept() {
		DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
		var documentsForQuery = new HashMap<Query, List<List<Document>>>();
		documentsForQuery.put(new Query("query1"),
				List.of(List.of(new Document("1", "Content 1", Map.of()), new Document("2", "Content 2", Map.of())),
						List.of(new Document("3", "Content 3", Map.of()))));
		documentsForQuery.put(new Query("query2"),
				List.of(List.of(new Document("2", "Content 2", Map.of()), new Document("4", "Content 4", Map.of()))));

		List<Document> result = documentJoiner.join(documentsForQuery);

		assertThat(result).hasSize(4);
		assertThat(result).extracting(Document::getId).containsExactlyInAnyOrder("1", "2", "3", "4");
		assertThat(result).extracting(Document::getText).containsOnlyOnce("Content 2");
	}

}
