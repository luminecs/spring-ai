package org.springframework.ai.reader.pdf.config;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.util.Assert;

public final class PdfDocumentReaderConfig {

	public static final int ALL_PAGES = 0;

	public final boolean reversedParagraphPosition;

	public final int pagesPerDocument;

	public final int pageTopMargin;

	public final int pageBottomMargin;

	public final ExtractedTextFormatter pageExtractedTextFormatter;

	private PdfDocumentReaderConfig(PdfDocumentReaderConfig.Builder builder) {
		this.pagesPerDocument = builder.pagesPerDocument;
		this.pageBottomMargin = builder.pageBottomMargin;
		this.pageTopMargin = builder.pageTopMargin;
		this.pageExtractedTextFormatter = builder.pageExtractedTextFormatter;
		this.reversedParagraphPosition = builder.reversedParagraphPosition;
	}

	public static PdfDocumentReaderConfig.Builder builder() {

		return new Builder();
	}

	public static PdfDocumentReaderConfig defaultConfig() {
		return builder().build();
	}

	public static final class Builder {

		private int pagesPerDocument = 1;

		private int pageTopMargin = 0;

		private int pageBottomMargin = 0;

		private ExtractedTextFormatter pageExtractedTextFormatter = ExtractedTextFormatter.defaults();

		private boolean reversedParagraphPosition = false;

		private Builder() {
		}

		public PdfDocumentReaderConfig.Builder withPageExtractedTextFormatter(
				ExtractedTextFormatter pageExtractedTextFormatter) {
			Assert.notNull(pageExtractedTextFormatter, "PageExtractedTextFormatter must not be null.");
			this.pageExtractedTextFormatter = pageExtractedTextFormatter;
			return this;
		}

		public PdfDocumentReaderConfig.Builder withPagesPerDocument(int pagesPerDocument) {
			Assert.isTrue(pagesPerDocument >= 0, "Page count must be a positive value.");
			this.pagesPerDocument = pagesPerDocument;
			return this;
		}

		public PdfDocumentReaderConfig.Builder withPageTopMargin(int topMargin) {
			Assert.isTrue(topMargin >= 0, "Page margins must be a positive value.");
			this.pageTopMargin = topMargin;
			return this;
		}

		public PdfDocumentReaderConfig.Builder withPageBottomMargin(int bottomMargin) {
			Assert.isTrue(bottomMargin >= 0, "Page margins must be a positive value.");
			this.pageBottomMargin = bottomMargin;
			return this;
		}

		public Builder withReversedParagraphPosition(boolean reversedParagraphPosition) {
			this.reversedParagraphPosition = reversedParagraphPosition;
			return this;
		}

		public PdfDocumentReaderConfig build() {
			return new PdfDocumentReaderConfig(this);
		}

	}

}
