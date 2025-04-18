package org.springframework.ai.reader;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

public class TextReaderTests {

	@Test
	void loadText() {
		Resource resource = new DefaultResourceLoader().getResource("classpath:text_source.txt");
		assertThat(resource).isNotNull();
		TextReader textReader = new TextReader(resource);
		textReader.getCustomMetadata().put("customKey", "Value");

		List<Document> documents0 = textReader.get();

		List<Document> documents = new TokenTextSplitter().apply(documents0);

		assertThat(documents.size()).isEqualTo(54);

		for (Document document : documents) {
			assertThat(document.getMetadata().get("customKey")).isEqualTo("Value");
			assertThat(document.getMetadata().get(TextReader.SOURCE_METADATA)).isEqualTo("text_source.txt");
			assertThat(document.getMetadata().get(TextReader.CHARSET_METADATA)).isEqualTo("UTF-8");
			assertThat(document.getText()).isNotEmpty();
		}
	}

	@Test
	void loadTextFromByteArrayResource() {

		Resource defaultByteArrayResource = new ByteArrayResource("Test content".getBytes(StandardCharsets.UTF_8));
		assertThat(defaultByteArrayResource).isNotNull();
		TextReader defaultTextReader = new TextReader(defaultByteArrayResource);
		defaultTextReader.getCustomMetadata().put("customKey", "DefaultValue");

		List<Document> defaultDocuments = defaultTextReader.get();

		assertThat(defaultDocuments).hasSize(1);

		Document defaultDocument = defaultDocuments.get(0);
		assertThat(defaultDocument.getMetadata()).containsEntry("customKey", "DefaultValue")
			.containsEntry(TextReader.CHARSET_METADATA, "UTF-8");

		assertThat(defaultDocument.getMetadata().get(TextReader.SOURCE_METADATA))
			.isEqualTo("Byte array resource [resource loaded from byte array]");

		assertThat(defaultDocument.getText()).isEqualTo("Test content");

		String customDescription = "Custom byte array resource";
		Resource customByteArrayResource = new ByteArrayResource(
				"Another test content".getBytes(StandardCharsets.UTF_8), customDescription);
		assertThat(customByteArrayResource).isNotNull();
		TextReader customTextReader = new TextReader(customByteArrayResource);
		customTextReader.getCustomMetadata().put("customKey", "CustomValue");

		List<Document> customDocuments = customTextReader.get();

		assertThat(customDocuments).hasSize(1);

		Document customDocument = customDocuments.get(0);
		assertThat(customDocument.getMetadata()).containsEntry("customKey", "CustomValue")
			.containsEntry(TextReader.CHARSET_METADATA, "UTF-8");

		assertThat(customDocument.getMetadata().get(TextReader.SOURCE_METADATA))
			.isEqualTo("Byte array resource [Custom byte array resource]");

		assertThat(customDocument.getText()).isEqualTo("Another test content");
	}

}
