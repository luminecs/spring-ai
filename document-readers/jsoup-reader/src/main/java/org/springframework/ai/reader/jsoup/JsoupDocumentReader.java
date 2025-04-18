package org.springframework.ai.reader.jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class JsoupDocumentReader implements DocumentReader {

	private final Resource htmlResource;

	private final JsoupDocumentReaderConfig config;

	public JsoupDocumentReader(String htmlResource) {
		this(new DefaultResourceLoader().getResource(htmlResource));
	}

	public JsoupDocumentReader(Resource htmlResource) {
		this(htmlResource, JsoupDocumentReaderConfig.defaultConfig());
	}

	public JsoupDocumentReader(String htmlResource, JsoupDocumentReaderConfig config) {
		this(new DefaultResourceLoader().getResource(htmlResource), config);
	}

	public JsoupDocumentReader(Resource htmlResource, JsoupDocumentReaderConfig config) {
		this.htmlResource = htmlResource;
		this.config = config;
	}

	@Override
	public List<Document> get() {
		try (InputStream inputStream = this.htmlResource.getInputStream()) {
			org.jsoup.nodes.Document doc = Jsoup.parse(inputStream, this.config.charset, "");

			List<Document> documents = new ArrayList<>();

			if (this.config.allElements) {

				String allText = doc.body().text();
				Document document = new Document(allText);
				addMetadata(doc, document);
				documents.add(document);
			}
			else if (this.config.groupByElement) {

				Elements selectedElements = doc.select(this.config.selector);
				for (Element element : selectedElements) {
					String elementText = element.text();
					Document document = new Document(elementText);
					addMetadata(doc, document);

					documents.add(document);
				}
			}
			else {

				Elements elements = doc.select(this.config.selector);
				String text = elements.stream().map(Element::text).collect(Collectors.joining(this.config.separator));
				Document document = new Document(text);
				addMetadata(doc, document);
				documents.add(document);
			}

			return documents;

		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read HTML resource: " + this.htmlResource, e);
		}
	}

	private void addMetadata(org.jsoup.nodes.Document jsoupDoc, Document springDoc) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("title", jsoupDoc.title());

		for (String metaTag : this.config.metadataTags) {
			String value = jsoupDoc.select("meta[name=" + metaTag + "]").attr("content");
			if (!value.isEmpty()) {
				metadata.put(metaTag, value);
			}
		}

		if (this.config.includeLinkUrls) {
			Elements links = jsoupDoc.select("a[href]");
			List<String> linkUrls = links.stream().map(link -> link.attr("abs:href")).toList();
			metadata.put("linkUrls", linkUrls);
		}

		metadata.putAll(this.config.additionalMetadata);

		springDoc.getMetadata().putAll(metadata);
	}

}
