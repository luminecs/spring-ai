package org.springframework.ai.embedding;

import java.util.List;

import org.springframework.ai.model.ModelRequest;
import org.springframework.lang.Nullable;

public class EmbeddingRequest implements ModelRequest<List<String>> {

	private final List<String> inputs;

	@Nullable
	private final EmbeddingOptions options;

	public EmbeddingRequest(List<String> inputs, @Nullable EmbeddingOptions options) {
		this.inputs = inputs;
		this.options = options;
	}

	@Override
	public List<String> getInstructions() {
		return this.inputs;
	}

	@Override
	@Nullable
	public EmbeddingOptions getOptions() {
		return this.options;
	}

}
