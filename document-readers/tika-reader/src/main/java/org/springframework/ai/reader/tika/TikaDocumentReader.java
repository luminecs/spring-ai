package org.springframework.ai.reader.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

public class TikaDocumentReader implements DocumentReader {

	public static final String METADATA_SOURCE = "source";

	private final AutoDetectParser parser;

	private final ContentHandler handler;

	private final Metadata metadata;

	private final ParseContext context;

	private final Resource resource;

	private final ExtractedTextFormatter textFormatter;

	public TikaDocumentReader(String resourceUrl) {
		this(resourceUrl, ExtractedTextFormatter.defaults());
	}

	public TikaDocumentReader(String resourceUrl, ExtractedTextFormatter textFormatter) {
		this(new DefaultResourceLoader().getResource(resourceUrl), textFormatter);
	}

	public TikaDocumentReader(Resource resource) {
		this(resource, ExtractedTextFormatter.defaults());
	}

	public TikaDocumentReader(Resource resource, ExtractedTextFormatter textFormatter) {
		this(resource, new BodyContentHandler(-1), textFormatter);
	}

	public TikaDocumentReader(Resource resource, ContentHandler contentHandler, ExtractedTextFormatter textFormatter) {
		this.parser = new AutoDetectParser();
		this.handler = contentHandler;
		this.metadata = new Metadata();
		this.context = new ParseContext();
		this.resource = resource;
		this.textFormatter = textFormatter;
	}

	@Override
	public List<Document> get() {
		try (InputStream stream = this.resource.getInputStream()) {
			this.parser.parse(stream, this.handler, this.metadata, this.context);
			return List.of(toDocument(this.handler.toString()));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Document toDocument(String docText) {
		docText = Objects.requireNonNullElse(docText, "");
		docText = this.textFormatter.format(docText);
		Document doc = new Document(docText);
		doc.getMetadata().put(METADATA_SOURCE, resourceName());
		return doc;
	}

	private String resourceName() {
		try {
			var resourceName = this.resource.getFilename();
			if (!StringUtils.hasText(resourceName)) {
				resourceName = this.resource.getURI().toString();
			}
			return resourceName;
		}
		catch (IOException e) {
			return String.format("Invalid source URI: %s", e.getMessage());
		}
	}

}
