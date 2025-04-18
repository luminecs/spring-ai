package org.springframework.ai.reader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

public class TextReader implements DocumentReader {

	public static final String CHARSET_METADATA = "charset";

	public static final String SOURCE_METADATA = "source";

	private final Resource resource;

	private final Map<String, Object> customMetadata = new HashMap<>();

	private Charset charset = StandardCharsets.UTF_8;

	public TextReader(String resourceUrl) {
		this(new DefaultResourceLoader().getResource(resourceUrl));
	}

	public TextReader(Resource resource) {
		Objects.requireNonNull(resource, "The Spring Resource must not be null");
		this.resource = resource;
	}

	public Charset getCharset() {
		return this.charset;
	}

	public void setCharset(Charset charset) {
		Objects.requireNonNull(charset, "The charset must not be null");
		this.charset = charset;
	}

	public Map<String, Object> getCustomMetadata() {
		return this.customMetadata;
	}

	@Override
	public List<Document> get() {
		try {

			String document = StreamUtils.copyToString(this.resource.getInputStream(), this.charset);

			this.customMetadata.put(CHARSET_METADATA, this.charset.name());
			this.customMetadata.put(SOURCE_METADATA, this.resource.getFilename());
			this.customMetadata.put(SOURCE_METADATA, getResourceIdentifier(this.resource));

			return List.of(new Document(document, this.customMetadata));

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getResourceIdentifier(Resource resource) {

		String filename = resource.getFilename();
		if (filename != null && !filename.isEmpty()) {
			return filename;
		}

		try {
			URI uri = resource.getURI();
			if (uri != null) {
				return uri.toString();
			}
		}
		catch (IOException ignored) {

		}

		try {
			URL url = resource.getURL();
			if (url != null) {
				return url.toString();
			}
		}
		catch (IOException ignored) {

		}

		return resource.getDescription();
	}

}
