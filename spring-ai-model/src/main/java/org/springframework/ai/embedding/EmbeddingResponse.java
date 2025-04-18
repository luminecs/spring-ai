package org.springframework.ai.embedding;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.model.ModelResponse;
import org.springframework.util.Assert;

public class EmbeddingResponse implements ModelResponse<Embedding> {

	private final List<Embedding> embeddings;

	private final EmbeddingResponseMetadata metadata;

	public EmbeddingResponse(List<Embedding> embeddings) {
		this(embeddings, new EmbeddingResponseMetadata());
	}

	public EmbeddingResponse(List<Embedding> embeddings, EmbeddingResponseMetadata metadata) {
		this.embeddings = embeddings;
		this.metadata = metadata;
	}

	public EmbeddingResponseMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public Embedding getResult() {
		Assert.notEmpty(this.embeddings, "No embedding data available.");
		return this.embeddings.get(0);
	}

	@Override
	public List<Embedding> getResults() {
		return this.embeddings;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EmbeddingResponse that = (EmbeddingResponse) o;
		return Objects.equals(this.embeddings, that.embeddings) && Objects.equals(this.metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.embeddings, this.metadata);
	}

	@Override
	public String toString() {
		return "EmbeddingResult{" + "data=" + this.embeddings + ", metadata=" + this.metadata + '}';
	}

}
