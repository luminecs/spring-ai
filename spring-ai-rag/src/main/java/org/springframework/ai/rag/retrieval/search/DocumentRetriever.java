package org.springframework.ai.rag.retrieval.search;

import java.util.List;
import java.util.function.Function;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

public interface DocumentRetriever extends Function<Query, List<Document>> {

	List<Document> retrieve(Query query);

	default List<Document> apply(Query query) {
		return retrieve(query);
	}

}
