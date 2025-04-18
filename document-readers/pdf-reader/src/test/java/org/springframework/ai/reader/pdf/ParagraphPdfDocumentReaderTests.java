package org.springframework.ai.reader.pdf;

import org.junit.jupiter.api.Test;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParagraphPdfDocumentReaderTests {

	@Test
	public void testPdfWithoutToc() {

		assertThatThrownBy(() ->

		new ParagraphPdfDocumentReader("classpath:/sample1.pdf",
				PdfDocumentReaderConfig.builder()
					.withPageTopMargin(0)
					.withPageBottomMargin(0)
					.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
						.withNumberOfTopTextLinesToDelete(0)
						.withNumberOfBottomTextLinesToDelete(3)
						.withNumberOfTopPagesToSkipBeforeDelete(0)
						.build())
					.withPagesPerDocument(1)
					.build()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
					"Document outline (e.g. TOC) is null. Make sure the PDF document has a table of contents (TOC). If not, consider the PagePdfDocumentReader or the TikaDocumentReader instead.");

	}

}
