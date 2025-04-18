package org.springframework.ai.vectorstore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.content.Content;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.document.id.IdGenerator;
import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.util.Assert;

public final class SimpleVectorStoreContent implements Content {

	private final String id;

	private final String text;

	private final Map<String, Object> metadata;

	private final float[] embedding;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public SimpleVectorStoreContent(@JsonProperty("text") @JsonAlias({ "content" }) String text,
			@JsonProperty("embedding") float[] embedding) {
		this(text, new HashMap<>(), embedding);
	}

	public SimpleVectorStoreContent(String text, Map<String, Object> metadata, float[] embedding) {
		this(text, metadata, new RandomIdGenerator(), embedding);
	}

	public SimpleVectorStoreContent(String text, Map<String, Object> metadata, IdGenerator idGenerator,
			float[] embedding) {
		this(idGenerator.generateId(text, metadata), text, metadata, embedding);
	}

	public SimpleVectorStoreContent(String id, String text, Map<String, Object> metadata, float[] embedding) {
		Assert.hasText(id, "id must not be null or empty");
		Assert.notNull(text, "content must not be null");
		Assert.notNull(metadata, "metadata must not be null");
		Assert.notNull(embedding, "embedding must not be null");
		Assert.isTrue(embedding.length > 0, "embedding vector must not be empty");

		this.id = id;
		this.text = text;
		this.metadata = Map.copyOf(metadata);
		this.embedding = Arrays.copyOf(embedding, embedding.length);
	}

	@Deprecated(forRemoval = true, since = "1.0.0-M7")
	public SimpleVectorStoreContent withEmbedding(float[] embedding) {
		Assert.notNull(embedding, "embedding must not be null");
		Assert.isTrue(embedding.length > 0, "embedding vector must not be empty");
		return new SimpleVectorStoreContent(this.id, this.text, this.metadata, embedding);
	}

	public String getId() {
		return this.id;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	public float[] getEmbedding() {
		return Arrays.copyOf(this.embedding, this.embedding.length);
	}

	public Document toDocument(Double score) {
		var metadata = new HashMap<>(this.metadata);
		metadata.put(DocumentMetadata.DISTANCE.value(), 1.0 - score);
		return Document.builder().id(this.id).text(this.text).metadata(metadata).score(score).build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SimpleVectorStoreContent that = (SimpleVectorStoreContent) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.text, that.text)
				&& Objects.equals(this.metadata, that.metadata) && Arrays.equals(this.embedding, that.embedding);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(this.id);
		result = 31 * result + Objects.hashCode(this.text);
		result = 31 * result + Objects.hashCode(this.metadata);
		result = 31 * result + Arrays.hashCode(this.embedding);
		return result;
	}

	@Override
	public String toString() {
		return "SimpleVectorStoreContent{" + "id='" + this.id + '\'' + ", content='" + this.text + '\'' + ", metadata="
				+ this.metadata + ", embedding=" + Arrays.toString(this.embedding) + '}';
	}

}
