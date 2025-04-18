package org.springframework.ai.document;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentFormatterTests {

	Document document = new Document("The World is Big and Salvation Lurks Around the Corner",
			Map.of("embedKey1", "value1", "embedKey2", "value2", "embedKey3", "value3", "llmKey2", "value4"));

	@Test
	public void noExplicitlySetFormatter() {
		assertThat(this.document.getText()).isEqualTo("""
				The World is Big and Salvation Lurks Around the Corner""");

		assertThat(this.document.getFormattedContent()).isEqualTo(this.document.getFormattedContent(MetadataMode.ALL));
		assertThat(this.document.getFormattedContent())
			.isEqualTo(this.document.getFormattedContent(Document.DEFAULT_CONTENT_FORMATTER, MetadataMode.ALL));

	}

	@Test
	public void defaultConfigTextFormatter() {

		DefaultContentFormatter defaultConfigFormatter = DefaultContentFormatter.defaultConfig();

		assertThat(this.document.getFormattedContent(defaultConfigFormatter, MetadataMode.ALL)).isEqualTo("""
				llmKey2: value4
				embedKey1: value1
				embedKey2: value2
				embedKey3: value3

				The World is Big and Salvation Lurks Around the Corner""");

		assertThat(this.document.getFormattedContent(defaultConfigFormatter, MetadataMode.ALL))
			.isEqualTo(this.document.getFormattedContent());

		assertThat(this.document.getFormattedContent(defaultConfigFormatter, MetadataMode.ALL))
			.isEqualTo(defaultConfigFormatter.format(this.document, MetadataMode.ALL));
	}

}
