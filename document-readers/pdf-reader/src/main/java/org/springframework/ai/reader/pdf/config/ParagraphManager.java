package org.springframework.ai.reader.pdf.config;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ParagraphManager {

	private final Paragraph rootParagraph;

	private final PDDocument document;

	public ParagraphManager(PDDocument document) {

		Assert.notNull(document, "PDDocument must not be null");
		Assert.notNull(document.getDocumentCatalog().getDocumentOutline(),
				"Document outline (e.g. TOC) is null. "
						+ "Make sure the PDF document has a table of contents (TOC). If not, consider the "
						+ "PagePdfDocumentReader or the TikaDocumentReader instead.");

		try {

			this.document = document;

			this.rootParagraph = this.generateParagraphs(
					new Paragraph(null, "root", -1, 1, this.document.getNumberOfPages(), 0),
					this.document.getDocumentCatalog().getDocumentOutline(), 0);

			printParagraph(this.rootParagraph, System.out);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public List<Paragraph> flatten() {
		List<Paragraph> paragraphs = new ArrayList<>();
		for (var child : this.rootParagraph.children()) {
			flatten(child, paragraphs);
		}
		return paragraphs;
	}

	private void flatten(Paragraph current, List<Paragraph> paragraphs) {
		paragraphs.add(current);
		for (var child : current.children()) {
			flatten(child, paragraphs);
		}
	}

	private void printParagraph(Paragraph paragraph, PrintStream printStream) {
		printStream.println(paragraph);
		for (Paragraph childParagraph : paragraph.children()) {
			printParagraph(childParagraph, printStream);
		}
	}

	protected Paragraph generateParagraphs(Paragraph parentParagraph, PDOutlineNode bookmark, Integer level)
			throws IOException {

		PDOutlineItem current = bookmark.getFirstChild();

		while (current != null) {

			int pageNumber = getPageNumber(current);
			var nextSiblingNumber = getPageNumber(current.getNextSibling());
			if (nextSiblingNumber < 0) {
				nextSiblingNumber = getPageNumber(current.getLastChild());
			}

			var paragraphPosition = (current.getDestination() instanceof PDPageXYZDestination)
					? ((PDPageXYZDestination) current.getDestination()).getTop() : 0;

			var currentParagraph = new Paragraph(parentParagraph, current.getTitle(), level, pageNumber,
					nextSiblingNumber, paragraphPosition);

			parentParagraph.children().add(currentParagraph);

			this.generateParagraphs(currentParagraph, current, level + 1);

			current = current.getNextSibling();
		}
		return parentParagraph;
	}

	private int getPageNumber(PDOutlineItem current) throws IOException {
		if (current == null) {
			return -1;
		}
		PDPage currentPage = current.findDestinationPage(this.document);
		PDPageTree pages = this.document.getDocumentCatalog().getPages();
		for (int i = 0; i < pages.getCount(); i++) {
			var page = pages.get(i);
			if (page.equals(currentPage)) {
				return i + 1;
			}
		}
		return -1;
	}

	public List<Paragraph> getParagraphsByLevel(Paragraph paragraph, int level, boolean interLevelText) {

		List<Paragraph> resultList = new ArrayList<>();

		if (paragraph.level() < level) {
			if (!CollectionUtils.isEmpty(paragraph.children())) {

				if (interLevelText) {
					var interLevelParagraph = new Paragraph(paragraph.parent(), paragraph.title(), paragraph.level(),
							paragraph.startPageNumber(), paragraph.children().get(0).startPageNumber(),
							paragraph.position());
					resultList.add(interLevelParagraph);
				}

				for (Paragraph child : paragraph.children()) {
					resultList.addAll(getParagraphsByLevel(child, level, interLevelText));
				}
			}
		}
		else if (paragraph.level() == level) {
			resultList.add(paragraph);
		}

		return resultList;
	}

	public record Paragraph(Paragraph parent, String title, int level, int startPageNumber, int endPageNumber,
			int position, List<Paragraph> children) {

		public Paragraph(Paragraph parent, String title, int level, int startPageNumber, int endPageNumber,
				int position) {
			this(parent, title, level, startPageNumber, endPageNumber, position, new ArrayList<>());
		}

		@Override
		public String toString() {
			String indent = (this.level < 0) ? "" : new String(new char[this.level * 2]).replace('\0', ' ');

			return indent + " " + this.level + ") " + this.title + " [" + this.startPageNumber + ","
					+ this.endPageNumber + "], children = " + this.children.size() + ", pos = " + this.position;
		}

	}

}
