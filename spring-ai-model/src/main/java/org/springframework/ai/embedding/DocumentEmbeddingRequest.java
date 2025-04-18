package org.springframework.ai.embedding;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.model.ModelRequest;

public class DocumentEmbeddingRequest implements ModelRequest<List<Document>> {

	private final List<Document> inputs;

	private final EmbeddingOptions options;

	public DocumentEmbeddingRequest(Document... inputs) {
		this(Arrays.asList(inputs), EmbeddingOptionsBuilder.builder().build());
	}

	public DocumentEmbeddingRequest(List<Document> inputs) {
		this(inputs, EmbeddingOptionsBuilder.builder().build());
	}

	public DocumentEmbeddingRequest(List<Document> inputs, EmbeddingOptions options) {
		this.inputs = inputs;
		this.options = options;
	}

	@Override
	public List<Document> getInstructions() {
		return this.inputs;
	}

	@Override
	public EmbeddingOptions getOptions() {
		return this.options;
	}

}
