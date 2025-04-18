package org.springframework.ai.writer;

import java.io.FileWriter;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.document.MetadataMode;
import org.springframework.util.Assert;

public class FileDocumentWriter implements DocumentWriter {

	public static final String METADATA_START_PAGE_NUMBER = "page_number";

	public static final String METADATA_END_PAGE_NUMBER = "end_page_number";

	private final String fileName;

	private final boolean withDocumentMarkers;

	private final MetadataMode metadataMode;

	private final boolean append;

	public FileDocumentWriter(String fileName) {
		this(fileName, false, MetadataMode.NONE, false);
	}

	public FileDocumentWriter(String fileName, boolean withDocumentMarkers) {
		this(fileName, withDocumentMarkers, MetadataMode.NONE, false);
	}

	public FileDocumentWriter(String fileName, boolean withDocumentMarkers, MetadataMode metadataMode, boolean append) {
		Assert.hasText(fileName, "File name must have a text.");
		Assert.notNull(metadataMode, "MetadataMode must not be null.");

		this.fileName = fileName;
		this.withDocumentMarkers = withDocumentMarkers;
		this.metadataMode = metadataMode;
		this.append = append;
	}

	@Override
	public void accept(List<Document> docs) {

		try (var writer = new FileWriter(this.fileName, this.append)) {

			int index = 0;
			for (Document doc : docs) {
				if (this.withDocumentMarkers) {
					writer.write(String.format("%n### Doc: %s, pages:[%s,%s]\n", index,
							doc.getMetadata().get(METADATA_START_PAGE_NUMBER),
							doc.getMetadata().get(METADATA_END_PAGE_NUMBER)));
				}
				writer.write(doc.getFormattedContent(this.metadataMode));
				index++;
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
