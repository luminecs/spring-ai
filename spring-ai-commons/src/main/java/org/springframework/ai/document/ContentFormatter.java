package org.springframework.ai.document;

public interface ContentFormatter {

	String format(Document document, MetadataMode mode);

}
