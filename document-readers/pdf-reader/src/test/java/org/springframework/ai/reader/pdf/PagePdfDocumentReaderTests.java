package org.springframework.ai.reader.pdf;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;

import static org.assertj.core.api.Assertions.assertThat;

class PagePdfDocumentReaderTests {

	@Test
	void classpathRead() {

		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/sample1.pdf",
				PdfDocumentReaderConfig.builder()
					.withPageTopMargin(0)
					.withPageBottomMargin(0)
					.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
						.withNumberOfTopTextLinesToDelete(0)
						.withNumberOfBottomTextLinesToDelete(3)
						.withNumberOfTopPagesToSkipBeforeDelete(0)
						.overrideLineSeparator("\n")
						.build())
					.withPagesPerDocument(1)
					.build());

		List<Document> docs = pdfReader.get();

		assertThat(docs).hasSize(4);

		String allText = docs.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));

		assertThat(allText).doesNotContain(
				List.of("Page  1 of 4", "Page  2 of 4", "Page  3 of 4", "Page  4 of 4", "PDF  Bookmark   Sample"));
	}

	@Test
	void testIndexOutOfBound() {
		var documents = new PagePdfDocumentReader("classpath:/sample2.pdf",
				PdfDocumentReaderConfig.builder()
					.withPageExtractedTextFormatter(ExtractedTextFormatter.builder().build())
					.withPagesPerDocument(1)
					.build())
			.get();

		assertThat(documents).hasSize(64);
	}

}
