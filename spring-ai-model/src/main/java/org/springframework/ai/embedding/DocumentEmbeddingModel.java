package org.springframework.ai.embedding;

import org.springframework.ai.model.Model;

public interface DocumentEmbeddingModel extends Model<DocumentEmbeddingRequest, EmbeddingResponse> {

	@Override
	EmbeddingResponse call(DocumentEmbeddingRequest request);

	int dimensions();

}
