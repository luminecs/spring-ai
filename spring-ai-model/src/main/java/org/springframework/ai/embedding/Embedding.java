package org.springframework.ai.embedding;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.ai.model.ModelResult;

public class Embedding implements ModelResult<float[]> {

	private final float[] embedding;

	private final Integer index;

	private final EmbeddingResultMetadata metadata;

	public Embedding(float[] embedding, Integer index) {
		this(embedding, index, EmbeddingResultMetadata.EMPTY);
	}

	public Embedding(float[] embedding, Integer index, EmbeddingResultMetadata metadata) {
		this.embedding = embedding;
		this.index = index;
		this.metadata = metadata;
	}

	@Override
	public float[] getOutput() {
		return this.embedding;
	}

	public Integer getIndex() {
		return this.index;
	}

	public EmbeddingResultMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Embedding other = (Embedding) o;
		return Arrays.equals(this.embedding, other.embedding) && Objects.equals(this.index, other.index);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(this.embedding), this.index);
	}

	@Override
	public String toString() {
		String message = this.embedding.length == 0 ? "<empty>" : "<has data>";
		return "Embedding{" + "embedding=" + message + ", index=" + this.index + '}';
	}

}
