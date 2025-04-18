package org.springframework.ai.document;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.id.IdGenerator;
import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@JsonIgnoreProperties({ "contentFormatter", "embedding" })
public class Document {

	public static final ContentFormatter DEFAULT_CONTENT_FORMATTER = DefaultContentFormatter.defaultConfig();

	private final String id;

	private final String text;

	private final Media media;

	private final Map<String, Object> metadata;

	@Nullable
	private final Double score;

	@JsonIgnore
	private ContentFormatter contentFormatter = DEFAULT_CONTENT_FORMATTER;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public Document(@JsonProperty("content") String content) {
		this(content, new HashMap<>());
	}

	public Document(String text, Map<String, Object> metadata) {
		this(new RandomIdGenerator().generateId(), text, null, metadata, null);
	}

	public Document(String id, String text, Map<String, Object> metadata) {
		this(id, text, null, metadata, null);
	}

	public Document(Media media, Map<String, Object> metadata) {
		this(new RandomIdGenerator().generateId(), null, media, metadata, null);
	}

	public Document(String id, Media media, Map<String, Object> metadata) {
		this(id, null, media, metadata, null);
	}

	private Document(String id, String text, Media media, Map<String, Object> metadata, @Nullable Double score) {
		Assert.hasText(id, "id cannot be null or empty");
		Assert.notNull(metadata, "metadata cannot be null");
		Assert.noNullElements(metadata.keySet(), "metadata cannot have null keys");
		Assert.noNullElements(metadata.values(), "metadata cannot have null values");
		Assert.isTrue(text != null ^ media != null, "exactly one of text or media must be specified");

		this.id = id;
		this.text = text;
		this.media = media;
		this.metadata = new HashMap<>(metadata);
		this.score = score;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getId() {
		return this.id;
	}

	@Nullable
	public String getText() {
		return this.text;
	}

	public boolean isText() {
		return this.text != null;
	}

	@Nullable
	public Media getMedia() {
		return this.media;
	}

	@JsonIgnore
	public String getFormattedContent() {
		return this.getFormattedContent(MetadataMode.ALL);
	}

	public String getFormattedContent(MetadataMode metadataMode) {
		Assert.notNull(metadataMode, "Metadata mode must not be null");
		return this.contentFormatter.format(this, metadataMode);
	}

	public String getFormattedContent(ContentFormatter formatter, MetadataMode metadataMode) {
		Assert.notNull(formatter, "formatter must not be null");
		Assert.notNull(metadataMode, "Metadata mode must not be null");
		return formatter.format(this, metadataMode);
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	@Nullable
	public Double getScore() {
		return this.score;
	}

	@Deprecated(since = "1.0.0-M4")
	public ContentFormatter getContentFormatter() {
		return this.contentFormatter;
	}

	public void setContentFormatter(ContentFormatter contentFormatter) {
		this.contentFormatter = contentFormatter;
	}

	public Builder mutate() {
		return new Builder().id(this.id).text(this.text).media(this.media).metadata(this.metadata).score(this.score);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		Document document = (Document) o;
		return Objects.equals(this.id, document.id) && Objects.equals(this.text, document.text)
				&& Objects.equals(this.media, document.media) && Objects.equals(this.metadata, document.metadata)
				&& Objects.equals(this.score, document.score);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.text, this.media, this.metadata, this.score);
	}

	@Override
	public String toString() {
		return "Document{" + "id='" + this.id + '\'' + ", text='" + this.text + '\'' + ", media='" + this.media + '\''
				+ ", metadata=" + this.metadata + ", score=" + this.score + '}';
	}

	public static class Builder {

		private String id;

		private String text;

		private Media media;

		private Map<String, Object> metadata = new HashMap<>();

		@Nullable
		private Double score;

		private IdGenerator idGenerator = new RandomIdGenerator();

		public Builder idGenerator(IdGenerator idGenerator) {
			Assert.notNull(idGenerator, "idGenerator cannot be null");
			this.idGenerator = idGenerator;
			return this;
		}

		public Builder id(String id) {
			Assert.hasText(id, "id cannot be null or empty");
			this.id = id;
			return this;
		}

		public Builder text(@Nullable String text) {
			this.text = text;
			return this;
		}

		public Builder media(@Nullable Media media) {
			this.media = media;
			return this;
		}

		public Builder metadata(Map<String, Object> metadata) {
			Assert.notNull(metadata, "metadata cannot be null");
			this.metadata = metadata;
			return this;
		}

		public Builder metadata(String key, Object value) {
			Assert.notNull(key, "metadata key cannot be null");
			Assert.notNull(value, "metadata value cannot be null");
			this.metadata.put(key, value);
			return this;
		}

		public Builder score(@Nullable Double score) {
			this.score = score;
			return this;
		}

		public Document build() {
			if (!StringUtils.hasText(this.id)) {
				this.id = this.idGenerator.generateId(this.text, this.metadata);
			}
			return new Document(this.id, this.text, this.media, this.metadata, this.score);
		}

	}

}
