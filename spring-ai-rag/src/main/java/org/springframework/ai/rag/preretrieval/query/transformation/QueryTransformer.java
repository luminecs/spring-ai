package org.springframework.ai.rag.preretrieval.query.transformation;

import java.util.function.Function;

import org.springframework.ai.rag.Query;

public interface QueryTransformer extends Function<Query, Query> {

	Query transform(Query query);

	default Query apply(Query query) {
		return transform(query);
	}

}
