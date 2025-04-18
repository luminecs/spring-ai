package org.springframework.ai.rag.preretrieval.query.expansion;

import java.util.List;
import java.util.function.Function;

import org.springframework.ai.rag.Query;

public interface QueryExpander extends Function<Query, List<Query>> {

	List<Query> expand(Query query);

	default List<Query> apply(Query query) {
		return expand(query);
	}

}
