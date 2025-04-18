package org.springframework.ai.vertexai.gemini;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public abstract class MimeTypeDetector {

	private static final Map<String, MimeType> GEMINI_MIME_TYPES = new HashMap<>();

	public static MimeType getMimeType(URL url) {
		return getMimeType(url.getFile());
	}

	public static MimeType getMimeType(URI uri) {
		return getMimeType(uri.toString());
	}

	public static MimeType getMimeType(File file) {
		return getMimeType(file.getAbsolutePath());
	}

	public static MimeType getMimeType(Path path) {
		return getMimeType(path.getFileName());
	}

	public static MimeType getMimeType(Resource resource) {
		try {
			return getMimeType(resource.getURI());
		}
		catch (IOException e) {
			throw new IllegalArgumentException(
					String.format("Unable to detect the MIME type of '%s'. Please provide it explicitly.",
							resource.getFilename()),
					e);
		}
	}

	public static MimeType getMimeType(String path) {

		int dotIndex = path.lastIndexOf('.');

		if (dotIndex != -1 && dotIndex < path.length() - 1) {
			String extension = path.substring(dotIndex + 1);
			MimeType customMimeType = GEMINI_MIME_TYPES.get(extension);
			if (customMimeType != null) {
				return customMimeType;
			}
		}

		throw new IllegalArgumentException(
				String.format("Unable to detect the MIME type of '%s'. Please provide it explicitly.", path));
	}

	static {

		GEMINI_MIME_TYPES.put("png", MimeTypeUtils.IMAGE_PNG);
		GEMINI_MIME_TYPES.put("jpeg", MimeTypeUtils.IMAGE_JPEG);
		GEMINI_MIME_TYPES.put("jpg", MimeTypeUtils.IMAGE_JPEG);
		GEMINI_MIME_TYPES.put("gif", MimeTypeUtils.IMAGE_GIF);
		GEMINI_MIME_TYPES.put("mov", new MimeType("video", "mov"));
		GEMINI_MIME_TYPES.put("mp4", new MimeType("video", "mp4"));
		GEMINI_MIME_TYPES.put("mpg", new MimeType("video", "mpg"));
		GEMINI_MIME_TYPES.put("avi", new MimeType("video", "avi"));
		GEMINI_MIME_TYPES.put("wmv", new MimeType("video", "wmv"));
		GEMINI_MIME_TYPES.put("mpegps", new MimeType("mpegps", "mp4"));
		GEMINI_MIME_TYPES.put("flv", new MimeType("video", "flv"));
	}

}
