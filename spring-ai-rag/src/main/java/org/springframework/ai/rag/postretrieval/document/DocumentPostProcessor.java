package org.springframework.ai.rag.postretrieval.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.function.BiFunction;

public interface DocumentPostProcessor extends BiFunction<Query, List<Document>, List<Document>> {

	List<Document> process(Query query, List<Document> documents);

	default List<Document> apply(Query query, List<Document> documents) {
		return process(query, documents);
	}

}
