package org.springframework.ai.rag.postretrieval.compression;

import java.util.List;
import java.util.function.BiFunction;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.postretrieval.selection.DocumentSelector;

public interface DocumentCompressor extends BiFunction<Query, List<Document>, List<Document>> {

	List<Document> compress(Query query, List<Document> documents);

	default List<Document> apply(Query query, List<Document> documents) {
		return compress(query, documents);
	}

}
