package org.springframework.ai.transformer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;

public class ContentFormatTransformer implements DocumentTransformer {

	private final boolean disableTemplateRewrite;

	private final ContentFormatter contentFormatter;

	public ContentFormatTransformer(ContentFormatter contentFormatter) {
		this(contentFormatter, false);
	}

	public ContentFormatTransformer(ContentFormatter contentFormatter, boolean disableTemplateRewrite) {
		this.contentFormatter = contentFormatter;
		this.disableTemplateRewrite = disableTemplateRewrite;
	}

	public List<Document> apply(List<Document> documents) {
		if (this.contentFormatter != null) {
			documents.forEach(this::processDocument);
		}

		return documents;
	}

	private void processDocument(Document document) {
		if (document.getContentFormatter() instanceof DefaultContentFormatter docFormatter
				&& this.contentFormatter instanceof DefaultContentFormatter toUpdateFormatter) {
			updateFormatter(document, docFormatter, toUpdateFormatter);

		}
		else {
			overrideFormatter(document);
		}
	}

	private void updateFormatter(Document document, DefaultContentFormatter docFormatter,
			DefaultContentFormatter toUpdateFormatter) {
		List<String> updatedEmbedExcludeKeys = new ArrayList<>(docFormatter.getExcludedEmbedMetadataKeys());
		updatedEmbedExcludeKeys.addAll(toUpdateFormatter.getExcludedEmbedMetadataKeys());

		List<String> updatedInterfaceExcludeKeys = new ArrayList<>(docFormatter.getExcludedInferenceMetadataKeys());
		updatedInterfaceExcludeKeys.addAll(toUpdateFormatter.getExcludedInferenceMetadataKeys());

		DefaultContentFormatter.Builder builder = DefaultContentFormatter.builder()
			.withExcludedEmbedMetadataKeys(updatedEmbedExcludeKeys)
			.withExcludedInferenceMetadataKeys(updatedInterfaceExcludeKeys)
			.withMetadataTemplate(docFormatter.getMetadataTemplate())
			.withMetadataSeparator(docFormatter.getMetadataSeparator());

		if (!this.disableTemplateRewrite) {
			builder.withTextTemplate(docFormatter.getTextTemplate());
		}

		document.setContentFormatter(builder.build());
	}

	private void overrideFormatter(Document document) {
		document.setContentFormatter(this.contentFormatter);
	}

}
