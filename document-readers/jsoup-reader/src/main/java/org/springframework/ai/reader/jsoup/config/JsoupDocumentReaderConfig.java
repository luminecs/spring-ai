package org.springframework.ai.reader.jsoup.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.util.Assert;

public final class JsoupDocumentReaderConfig {

	public final String charset;

	public final String selector;

	public final String separator;

	public final boolean allElements;

	public final boolean groupByElement;

	public final boolean includeLinkUrls;

	public final List<String> metadataTags;

	public final Map<String, Object> additionalMetadata;

	private JsoupDocumentReaderConfig(Builder builder) {
		this.charset = builder.charset;
		this.selector = builder.selector;
		this.separator = builder.separator;
		this.allElements = builder.allElements;
		this.includeLinkUrls = builder.includeLinkUrls;
		this.metadataTags = builder.metadataTags;
		this.groupByElement = builder.groupByElement;
		this.additionalMetadata = builder.additionalMetadata;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static JsoupDocumentReaderConfig defaultConfig() {
		return builder().build();
	}

	public static final class Builder {

		private String charset = "UTF-8";

		private String selector = "body";

		private String separator = "\n";

		private boolean allElements = false;

		private boolean includeLinkUrls = false;

		private List<String> metadataTags = new ArrayList<>(List.of("description", "keywords"));

		private boolean groupByElement = false;

		private Map<String, Object> additionalMetadata = new HashMap<>();

		private Builder() {
		}

		public Builder charset(String charset) {
			this.charset = charset;
			return this;
		}

		public Builder selector(String selector) {
			this.selector = selector;
			return this;
		}

		public Builder separator(String separator) {
			this.separator = separator;
			return this;
		}

		public Builder allElements(boolean allElements) {
			this.allElements = allElements;
			return this;
		}

		public Builder groupByElement(boolean groupByElement) {
			this.groupByElement = groupByElement;
			return this;
		}

		public Builder includeLinkUrls(boolean includeLinkUrls) {
			this.includeLinkUrls = includeLinkUrls;
			return this;
		}

		public Builder metadataTag(String metadataTag) {
			this.metadataTags.add(metadataTag);
			return this;
		}

		public Builder metadataTags(List<String> metadataTags) {
			this.metadataTags = new ArrayList<>(metadataTags);
			return this;
		}

		public Builder additionalMetadata(String key, Object value) {
			Assert.notNull(key, "key must not be null");
			Assert.notNull(value, "value must not be null");
			this.additionalMetadata.put(key, value);
			return this;
		}

		public Builder additionalMetadata(Map<String, Object> additionalMetadata) {
			Assert.notNull(additionalMetadata, "additionalMetadata must not be null");
			this.additionalMetadata = additionalMetadata;
			return this;
		}

		public JsoupDocumentReaderConfig build() {
			return new JsoupDocumentReaderConfig(this);
		}

	}

}
