package org.springframework.ai.rag.postretrieval.ranking;

import java.util.List;
import java.util.function.BiFunction;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.compression.DocumentCompressor;
import org.springframework.ai.rag.postretrieval.selection.DocumentSelector;

public interface DocumentRanker extends BiFunction<Query, List<Document>, List<Document>> {

	List<Document> rank(Query query, List<Document> documents);

	default List<Document> apply(Query query, List<Document> documents) {
		return rank(query, documents);
	}

}
