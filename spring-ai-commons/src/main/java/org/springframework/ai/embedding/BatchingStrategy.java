package org.springframework.ai.embedding;

import java.util.List;

import org.springframework.ai.document.Document;

public interface BatchingStrategy {

	List<List<Document>> batch(List<Document> documents);

}
