package org.springframework.ai.chat.messages;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

final class MessageUtils {

	private MessageUtils() {
	}

	static String readResource(Resource resource) {
		return readResource(resource, Charset.defaultCharset());
	}

	static String readResource(Resource resource, Charset charset) {
		Assert.notNull(resource, "resource cannot be null");
		Assert.notNull(charset, "charset cannot be null");
		try (InputStream inputStream = resource.getInputStream()) {
			return StreamUtils.copyToString(inputStream, charset);
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to read resource", ex);
		}
	}

}
