package org.springframework.ai.document;

import java.util.List;
import java.util.function.Consumer;

public interface DocumentWriter extends Consumer<List<Document>> {

	default void write(List<Document> documents) {
		accept(documents);
	}

}
