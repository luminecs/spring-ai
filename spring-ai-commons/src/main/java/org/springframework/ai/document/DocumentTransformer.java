package org.springframework.ai.document;

import java.util.List;
import java.util.function.Function;

public interface DocumentTransformer extends Function<List<Document>, List<Document>> {

	default List<Document> transform(List<Document> transform) {
		return apply(transform);
	}

}
