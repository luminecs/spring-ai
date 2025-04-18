package org.springframework.ai.reader;

import org.springframework.util.StringUtils;

public final class ExtractedTextFormatter {

	private final boolean leftAlignment;

	private final int numberOfTopPagesToSkipBeforeDelete;

	private final int numberOfTopTextLinesToDelete;

	private final int numberOfBottomTextLinesToDelete;

	private final String lineSeparator;

	private ExtractedTextFormatter(Builder builder) {
		this.leftAlignment = builder.leftAlignment;
		this.numberOfBottomTextLinesToDelete = builder.numberOfBottomTextLinesToDelete;
		this.numberOfTopPagesToSkipBeforeDelete = builder.numberOfTopPagesToSkipBeforeDelete;
		this.numberOfTopTextLinesToDelete = builder.numberOfTopTextLinesToDelete;
		this.lineSeparator = builder.lineSeparator;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static ExtractedTextFormatter defaults() {
		return new Builder().build();
	}

	public static String trimAdjacentBlankLines(String pageText) {
		return pageText.replaceAll("(?m)(^ *\n)", "\n").replaceAll("(?m)^$([\r\n]+?)(^$[\r\n]+?^)+", "$1");
	}

	public static String alignToLeft(String pageText) {
		return pageText.replaceAll("(?m)(^ *| +(?= |$))", "").replaceAll("(?m)^$(	?)(^$[\r\n]+?^)+", "$1");
	}

	public static String deleteBottomTextLines(String pageText, int numberOfLines, String lineSeparator) {
		if (!StringUtils.hasText(pageText)) {
			return pageText;
		}

		int lineCount = 0;
		int truncateIndex = pageText.length();
		int nextTruncateIndex = truncateIndex;
		while (lineCount < numberOfLines && nextTruncateIndex >= 0) {
			nextTruncateIndex = pageText.lastIndexOf(lineSeparator, truncateIndex - 1);
			truncateIndex = nextTruncateIndex < 0 ? truncateIndex : nextTruncateIndex;
			lineCount++;
		}
		return pageText.substring(0, truncateIndex);
	}

	public static String deleteTopTextLines(String pageText, int numberOfLines, String lineSeparator) {
		if (!StringUtils.hasText(pageText)) {
			return pageText;
		}
		int lineCount = 0;

		int truncateIndex = 0;
		int nextTruncateIndex = truncateIndex;
		while (lineCount < numberOfLines && nextTruncateIndex >= 0) {
			nextTruncateIndex = pageText.indexOf(lineSeparator, truncateIndex + 1);
			truncateIndex = nextTruncateIndex < 0 ? truncateIndex : nextTruncateIndex;
			lineCount++;
		}
		return pageText.substring(truncateIndex);
	}

	public String format(String pageText) {
		return this.format(pageText, 0);
	}

	public String format(String pageText, int pageNumber) {

		var text = trimAdjacentBlankLines(pageText);

		if (pageNumber >= this.numberOfTopPagesToSkipBeforeDelete) {
			text = deleteTopTextLines(text, this.numberOfTopTextLinesToDelete, this.lineSeparator);
			text = deleteBottomTextLines(text, this.numberOfBottomTextLinesToDelete, this.lineSeparator);
		}

		if (this.leftAlignment) {
			text = alignToLeft(text);
		}

		return text;
	}

	public static class Builder {

		private boolean leftAlignment = false;

		private int numberOfTopPagesToSkipBeforeDelete = 0;

		private int numberOfTopTextLinesToDelete = 0;

		private int numberOfBottomTextLinesToDelete = 0;

		private String lineSeparator = System.lineSeparator();

		public Builder withLeftAlignment(boolean leftAlignment) {
			this.leftAlignment = leftAlignment;
			return this;
		}

		public Builder withNumberOfTopPagesToSkipBeforeDelete(int numberOfTopPagesToSkipBeforeDelete) {
			this.numberOfTopPagesToSkipBeforeDelete = numberOfTopPagesToSkipBeforeDelete;
			return this;
		}

		public Builder withNumberOfTopTextLinesToDelete(int numberOfTopTextLinesToDelete) {
			this.numberOfTopTextLinesToDelete = numberOfTopTextLinesToDelete;
			return this;
		}

		public Builder withNumberOfBottomTextLinesToDelete(int numberOfBottomTextLinesToDelete) {
			this.numberOfBottomTextLinesToDelete = numberOfBottomTextLinesToDelete;
			return this;
		}

		public Builder overrideLineSeparator(String lineSeparator) {
			this.lineSeparator = lineSeparator;
			return this;
		}

		public ExtractedTextFormatter build() {
			return new ExtractedTextFormatter(this);
		}

	}

}
