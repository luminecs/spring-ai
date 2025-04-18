package org.springframework.ai.reader.pdf;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.pdf.layout.PDFLayoutTextStripperByArea;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class PagePdfDocumentReader implements DocumentReader {

	public static final String METADATA_START_PAGE_NUMBER = "page_number";

	public static final String METADATA_END_PAGE_NUMBER = "end_page_number";

	public static final String METADATA_FILE_NAME = "file_name";

	private static final String PDF_PAGE_REGION = "pdfPageRegion";

	protected final PDDocument document;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected String resourceFileName;

	private PdfDocumentReaderConfig config;

	public PagePdfDocumentReader(String resourceUrl) {
		this(new DefaultResourceLoader().getResource(resourceUrl));
	}

	public PagePdfDocumentReader(Resource pdfResource) {
		this(pdfResource, PdfDocumentReaderConfig.defaultConfig());
	}

	public PagePdfDocumentReader(String resourceUrl, PdfDocumentReaderConfig config) {
		this(new DefaultResourceLoader().getResource(resourceUrl), config);
	}

	public PagePdfDocumentReader(Resource pdfResource, PdfDocumentReaderConfig config) {
		try {
			PDFParser pdfParser = new PDFParser(
					new org.apache.pdfbox.io.RandomAccessReadBuffer(pdfResource.getInputStream()));
			this.document = pdfParser.parse();

			this.resourceFileName = pdfResource.getFilename();
			this.config = config;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Document> get() {

		List<Document> readDocuments = new ArrayList<>();
		try {
			var pdfTextStripper = new PDFLayoutTextStripperByArea();

			int pageNumber = 0;
			int pagesPerDocument = 0;
			int startPageNumber = pageNumber;

			List<String> pageTextGroupList = new ArrayList<>();

			int totalPages = this.document.getDocumentCatalog().getPages().getCount();
			int logFrequency = totalPages > 10 ? totalPages / 10 : 1;

			int counter = 0;

			PDPage lastPage = this.document.getDocumentCatalog().getPages().iterator().next();
			for (PDPage page : this.document.getDocumentCatalog().getPages()) {
				lastPage = page;
				if (counter % logFrequency == 0 && counter / logFrequency < 10) {
					logger.info("Processing PDF page: {}", (counter + 1));
				}
				counter++;

				pagesPerDocument++;

				if (this.config.pagesPerDocument != PdfDocumentReaderConfig.ALL_PAGES
						&& pagesPerDocument >= this.config.pagesPerDocument) {
					pagesPerDocument = 0;

					var aggregatedPageTextGroup = pageTextGroupList.stream().collect(Collectors.joining());
					if (StringUtils.hasText(aggregatedPageTextGroup)) {
						readDocuments.add(toDocument(page, aggregatedPageTextGroup, startPageNumber, pageNumber));
					}
					pageTextGroupList.clear();

					startPageNumber = pageNumber + 1;
				}
				int x0 = (int) page.getMediaBox().getLowerLeftX();
				int xW = (int) page.getMediaBox().getWidth();

				int y0 = (int) page.getMediaBox().getLowerLeftY() + this.config.pageTopMargin;
				int yW = (int) page.getMediaBox().getHeight()
						- (this.config.pageTopMargin + this.config.pageBottomMargin);

				pdfTextStripper.addRegion(PDF_PAGE_REGION, new Rectangle(x0, y0, xW, yW));
				pdfTextStripper.extractRegions(page);
				var pageText = pdfTextStripper.getTextForRegion(PDF_PAGE_REGION);

				if (StringUtils.hasText(pageText)) {

					pageText = this.config.pageExtractedTextFormatter.format(pageText, pageNumber);

					pageTextGroupList.add(pageText);
				}
				pageNumber++;
				pdfTextStripper.removeRegion(PDF_PAGE_REGION);
			}
			if (!CollectionUtils.isEmpty(pageTextGroupList)) {
				readDocuments.add(toDocument(lastPage, pageTextGroupList.stream().collect(Collectors.joining()),
						startPageNumber, pageNumber));
			}
			logger.info("Processing {} pages", totalPages);
			return readDocuments;

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Document toDocument(PDPage page, String docText, int startPageNumber, int endPageNumber) {
		Document doc = new Document(docText);
		doc.getMetadata().put(METADATA_START_PAGE_NUMBER, startPageNumber);
		if (startPageNumber != endPageNumber) {
			doc.getMetadata().put(METADATA_END_PAGE_NUMBER, endPageNumber);
		}
		doc.getMetadata().put(METADATA_FILE_NAME, this.resourceFileName);
		return doc;
	}

}
