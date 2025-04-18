package org.springframework.ai.rag.generation.augmentation;

import java.util.List;
import java.util.function.BiFunction;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

public interface QueryAugmenter extends BiFunction<Query, List<Document>, Query> {

	Query augment(Query query, List<Document> documents);

	default Query apply(Query query, List<Document> documents) {
		return augment(query, documents);
	}

}
