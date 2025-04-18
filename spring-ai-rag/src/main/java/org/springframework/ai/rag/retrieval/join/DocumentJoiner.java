package org.springframework.ai.rag.retrieval.join;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

public interface DocumentJoiner extends Function<Map<Query, List<List<Document>>>, List<Document>> {

	List<Document> join(Map<Query, List<List<Document>>> documentsForQuery);

	default List<Document> apply(Map<Query, List<List<Document>>> documentsForQuery) {
		return join(documentsForQuery);
	}

}
