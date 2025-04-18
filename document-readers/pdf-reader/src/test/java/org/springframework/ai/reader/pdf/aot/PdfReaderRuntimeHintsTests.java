package org.springframework.ai.reader.pdf.aot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;

import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.resource;

class PdfReaderRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints runtimeHints = new RuntimeHints();
		PdfReaderRuntimeHints pdfReaderRuntimeHints = new PdfReaderRuntimeHints();
		pdfReaderRuntimeHints.registerHints(runtimeHints, null);

		Assertions.assertThat(runtimeHints)
			.matches(resource().forResource("/org/apache/pdfbox/resources/glyphlist/zapfdingbats.txt"));
		Assertions.assertThat(runtimeHints)
			.matches(resource().forResource("/org/apache/pdfbox/resources/glyphlist/glyphlist.txt"));

		Assertions.assertThat(runtimeHints)
			.matches(resource().forResource("/org/apache/pdfbox/resources/version.properties"));
	}

}
