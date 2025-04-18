package org.springframework.ai.reader.markdown.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.util.Assert;

public class MarkdownDocumentReaderConfig {

	public final boolean horizontalRuleCreateDocument;

	public final boolean includeCodeBlock;

	public final boolean includeBlockquote;

	public final Map<String, Object> additionalMetadata;

	public MarkdownDocumentReaderConfig(Builder builder) {
		this.horizontalRuleCreateDocument = builder.horizontalRuleCreateDocument;
		this.includeCodeBlock = builder.includeCodeBlock;
		this.includeBlockquote = builder.includeBlockquote;
		this.additionalMetadata = builder.additionalMetadata;
	}

	public static MarkdownDocumentReaderConfig defaultConfig() {
		return builder().build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private boolean horizontalRuleCreateDocument = false;

		private boolean includeCodeBlock = false;

		private boolean includeBlockquote = false;

		private Map<String, Object> additionalMetadata = new HashMap<>();

		private Builder() {
		}

		public Builder withHorizontalRuleCreateDocument(boolean horizontalRuleCreateDocument) {
			this.horizontalRuleCreateDocument = horizontalRuleCreateDocument;
			return this;
		}

		public Builder withIncludeCodeBlock(boolean includeCodeBlock) {
			this.includeCodeBlock = includeCodeBlock;
			return this;
		}

		public Builder withIncludeBlockquote(boolean includeBlockquote) {
			this.includeBlockquote = includeBlockquote;
			return this;
		}

		public Builder withAdditionalMetadata(String key, Object value) {
			Assert.notNull(key, "key must not be null");
			Assert.notNull(value, "value must not be null");
			this.additionalMetadata.put(key, value);
			return this;
		}

		public Builder withAdditionalMetadata(Map<String, Object> additionalMetadata) {
			Assert.notNull(additionalMetadata, "additionalMetadata must not be null");
			this.additionalMetadata = additionalMetadata;
			return this;
		}

		public MarkdownDocumentReaderConfig build() {
			return new MarkdownDocumentReaderConfig(this);
		}

	}

}
