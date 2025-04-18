package org.springframework.ai.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

public final class DefaultContentFormatter implements ContentFormatter {

	private static final String TEMPLATE_CONTENT_PLACEHOLDER = "{content}";

	private static final String TEMPLATE_METADATA_STRING_PLACEHOLDER = "{metadata_string}";

	private static final String TEMPLATE_VALUE_PLACEHOLDER = "{value}";

	private static final String TEMPLATE_KEY_PLACEHOLDER = "{key}";

	private static final String DEFAULT_METADATA_TEMPLATE = String.format("%s: %s", TEMPLATE_KEY_PLACEHOLDER,
			TEMPLATE_VALUE_PLACEHOLDER);

	private static final String DEFAULT_METADATA_SEPARATOR = System.lineSeparator();

	private static final String DEFAULT_TEXT_TEMPLATE = String.format("%s\n\n%s", TEMPLATE_METADATA_STRING_PLACEHOLDER,
			TEMPLATE_CONTENT_PLACEHOLDER);

	private final String metadataTemplate;

	private final String metadataSeparator;

	private final String textTemplate;

	private final List<String> excludedInferenceMetadataKeys;

	private final List<String> excludedEmbedMetadataKeys;

	private DefaultContentFormatter(Builder builder) {
		this.metadataTemplate = builder.metadataTemplate;
		this.metadataSeparator = builder.metadataSeparator;
		this.textTemplate = builder.textTemplate;
		this.excludedInferenceMetadataKeys = builder.excludedInferenceMetadataKeys;
		this.excludedEmbedMetadataKeys = builder.excludedEmbedMetadataKeys;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static DefaultContentFormatter defaultConfig() {

		return builder().build();
	}

	@Override
	public String format(Document document, MetadataMode metadataMode) {

		var metadata = metadataFilter(document.getMetadata(), metadataMode);

		var metadataText = metadata.entrySet()
			.stream()
			.map(metadataEntry -> this.metadataTemplate.replace(TEMPLATE_KEY_PLACEHOLDER, metadataEntry.getKey())
				.replace(TEMPLATE_VALUE_PLACEHOLDER, metadataEntry.getValue().toString()))
			.collect(Collectors.joining(this.metadataSeparator));

		return this.textTemplate.replace(TEMPLATE_METADATA_STRING_PLACEHOLDER, metadataText)
			.replace(TEMPLATE_CONTENT_PLACEHOLDER, document.getText());
	}

	protected Map<String, Object> metadataFilter(Map<String, Object> metadata, MetadataMode metadataMode) {

		if (metadataMode == MetadataMode.ALL) {
			return new HashMap<String, Object>(metadata);
		}
		if (metadataMode == MetadataMode.NONE) {
			return new HashMap<String, Object>(Collections.emptyMap());
		}

		Set<String> usableMetadataKeys = new HashSet<>(metadata.keySet());

		if (metadataMode == MetadataMode.INFERENCE) {
			usableMetadataKeys.removeAll(this.excludedInferenceMetadataKeys);
		}
		else if (metadataMode == MetadataMode.EMBED) {
			usableMetadataKeys.removeAll(this.excludedEmbedMetadataKeys);
		}

		return new HashMap<String, Object>(metadata.entrySet()
			.stream()
			.filter(e -> usableMetadataKeys.contains(e.getKey()))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
	}

	public String getMetadataTemplate() {
		return this.metadataTemplate;
	}

	public String getMetadataSeparator() {
		return this.metadataSeparator;
	}

	public String getTextTemplate() {
		return this.textTemplate;
	}

	public List<String> getExcludedInferenceMetadataKeys() {
		return Collections.unmodifiableList(this.excludedInferenceMetadataKeys);
	}

	public List<String> getExcludedEmbedMetadataKeys() {
		return Collections.unmodifiableList(this.excludedEmbedMetadataKeys);
	}

	public static final class Builder {

		private String metadataTemplate = DEFAULT_METADATA_TEMPLATE;

		private String metadataSeparator = DEFAULT_METADATA_SEPARATOR;

		private String textTemplate = DEFAULT_TEXT_TEMPLATE;

		private List<String> excludedInferenceMetadataKeys = new ArrayList<>();

		private List<String> excludedEmbedMetadataKeys = new ArrayList<>();

		private Builder() {
		}

		public Builder from(DefaultContentFormatter fromFormatter) {
			this.withExcludedEmbedMetadataKeys(fromFormatter.getExcludedEmbedMetadataKeys())
				.withExcludedInferenceMetadataKeys(fromFormatter.getExcludedInferenceMetadataKeys())
				.withMetadataSeparator(fromFormatter.getMetadataSeparator())
				.withMetadataTemplate(fromFormatter.getMetadataTemplate())
				.withTextTemplate(fromFormatter.getTextTemplate());
			return this;
		}

		public Builder withMetadataTemplate(String metadataTemplate) {
			Assert.hasText(metadataTemplate, "Metadata Template must not be empty");
			this.metadataTemplate = metadataTemplate;
			return this;
		}

		public Builder withMetadataSeparator(String metadataSeparator) {
			Assert.notNull(metadataSeparator, "Metadata separator must not be empty");
			this.metadataSeparator = metadataSeparator;
			return this;
		}

		public Builder withTextTemplate(String textTemplate) {
			Assert.hasText(textTemplate, "Document's text template must not be empty");
			this.textTemplate = textTemplate;
			return this;
		}

		public Builder withExcludedInferenceMetadataKeys(List<String> excludedInferenceMetadataKeys) {
			Assert.notNull(excludedInferenceMetadataKeys, "Excluded inference metadata keys must not be null");
			this.excludedInferenceMetadataKeys = excludedInferenceMetadataKeys;
			return this;
		}

		public Builder withExcludedInferenceMetadataKeys(String... keys) {
			Assert.notNull(keys, "Excluded inference metadata keys must not be null");
			this.excludedInferenceMetadataKeys.addAll(Arrays.asList(keys));
			return this;
		}

		public Builder withExcludedEmbedMetadataKeys(List<String> excludedEmbedMetadataKeys) {
			Assert.notNull(excludedEmbedMetadataKeys, "Excluded Embed metadata keys must not be null");
			this.excludedEmbedMetadataKeys = excludedEmbedMetadataKeys;
			return this;
		}

		public Builder withExcludedEmbedMetadataKeys(String... keys) {
			Assert.notNull(keys, "Excluded Embed metadata keys must not be null");
			this.excludedEmbedMetadataKeys.addAll(Arrays.asList(keys));
			return this;
		}

		public DefaultContentFormatter build() {
			return new DefaultContentFormatter(this);
		}

	}

}
