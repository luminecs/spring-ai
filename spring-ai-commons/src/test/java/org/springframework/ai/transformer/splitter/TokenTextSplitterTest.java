package org.springframework.ai.transformer.splitter;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenTextSplitterTest {

	@Test
	public void testTokenTextSplitterBuilderWithDefaultValues() {

		var contentFormatter1 = DefaultContentFormatter.defaultConfig();
		var contentFormatter2 = DefaultContentFormatter.defaultConfig();

		assertThat(contentFormatter1).isNotSameAs(contentFormatter2);

		var doc1 = new Document("In the end, writing arises when man realizes that memory is not enough.",
				Map.of("key1", "value1", "key2", "value2"));
		doc1.setContentFormatter(contentFormatter1);

		var doc2 = new Document("The most oppressive thing about the labyrinth is that you are constantly "
				+ "being forced to choose. It isn’t the lack of an exit, but the abundance of exits that is so disorienting.",
				Map.of("key2", "value22", "key3", "value3"));
		doc2.setContentFormatter(contentFormatter2);

		var tokenTextSplitter = new TokenTextSplitter();

		var chunks = tokenTextSplitter.apply(List.of(doc1, doc2));

		assertThat(chunks.size()).isEqualTo(2);

		assertThat(chunks.get(0).getText())
			.isEqualTo("In the end, writing arises when man realizes that memory is not enough.");

		assertThat(chunks.get(1).getText()).isEqualTo(
				"The most oppressive thing about the labyrinth is that you are constantly being forced to choose. It isn’t the lack of an exit, but the abundance of exits that is so disorienting.");

		assertThat(chunks.get(0).getMetadata()).containsKeys("key1", "key2").doesNotContainKeys("key3");
		assertThat(chunks.get(1).getMetadata()).containsKeys("key2", "key3").doesNotContainKeys("key1");
	}

	@Test
	public void testTokenTextSplitterBuilderWithAllFields() {

		var contentFormatter1 = DefaultContentFormatter.defaultConfig();
		var contentFormatter2 = DefaultContentFormatter.defaultConfig();

		assertThat(contentFormatter1).isNotSameAs(contentFormatter2);

		var doc1 = new Document("In the end, writing arises when man realizes that memory is not enough.",
				Map.of("key1", "value1", "key2", "value2"));
		doc1.setContentFormatter(contentFormatter1);

		var doc2 = new Document("The most oppressive thing about the labyrinth is that you are constantly "
				+ "being forced to choose. It isn’t the lack of an exit, but the abundance of exits that is so disorienting.",
				Map.of("key2", "value22", "key3", "value3"));
		doc2.setContentFormatter(contentFormatter2);

		var tokenTextSplitter = TokenTextSplitter.builder()
			.withChunkSize(10)
			.withMinChunkSizeChars(5)
			.withMinChunkLengthToEmbed(3)
			.withMaxNumChunks(50)
			.withKeepSeparator(true)
			.build();

		var chunks = tokenTextSplitter.apply(List.of(doc1, doc2));

		assertThat(chunks.size()).isEqualTo(6);

		assertThat(chunks.get(0).getText()).isEqualTo("In the end, writing arises when man realizes that");
		assertThat(chunks.get(1).getText()).isEqualTo("memory is not enough.");

		assertThat(chunks.get(2).getText()).isEqualTo("The most oppressive thing about the labyrinth is that you");
		assertThat(chunks.get(3).getText()).isEqualTo("are constantly being forced to choose.");
		assertThat(chunks.get(4).getText()).isEqualTo("It isn’t the lack of an exit, but");
		assertThat(chunks.get(5).getText()).isEqualTo("the abundance of exits that is so disorienting");

		assertThat(chunks.get(0).getMetadata()).isEqualTo(chunks.get(1).getMetadata());
		assertThat(chunks.get(2).getMetadata()).isEqualTo(chunks.get(3).getMetadata());

		assertThat(chunks.get(0).getMetadata()).containsKeys("key1", "key2").doesNotContainKeys("key3");
		assertThat(chunks.get(2).getMetadata()).containsKeys("key2", "key3").doesNotContainKeys("key1");
	}

}
