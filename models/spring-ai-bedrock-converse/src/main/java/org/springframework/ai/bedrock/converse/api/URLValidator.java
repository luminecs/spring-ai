package org.springframework.ai.bedrock.converse.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

public final class URLValidator {

	private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)" +

			"((([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6})|" + "(localhost))" + "(:[0-9]{1,5})?" + "(/[\\w\\-./]*)*"
			+ "(\\?[\\w=&\\-.]*)?" + "(#[\\w-]*)?" + "$");

	private URLValidator() {

	}

	public static boolean isValidURLBasic(String urlString) {
		if (urlString == null || urlString.trim().isEmpty()) {
			return false;
		}
		return URL_PATTERN.matcher(urlString).matches();
	}

	public static boolean isValidURLStrict(String urlString) {
		if (urlString == null || urlString.trim().isEmpty()) {
			return false;
		}

		try {
			URL url = new URL(urlString);

			url.toURI();

			String protocol = url.getProtocol().toLowerCase();
			if (!protocol.equals("http") && !protocol.equals("https")) {
				return false;
			}

			String host = url.getHost();
			if (host == null || host.isEmpty()) {
				return false;
			}
			if (!host.equals("localhost") && !host.contains(".")) {
				return false;
			}

			int port = url.getPort();
			if (port != -1 && (port < 1 || port > 65535)) {
				return false;
			}

			return true;
		}
		catch (MalformedURLException | URISyntaxException e) {
			return false;
		}
	}

	public static String normalizeURL(String urlString) {
		if (urlString == null || urlString.trim().isEmpty()) {
			return null;
		}

		String normalized = urlString.trim();

		if (!normalized.toLowerCase().startsWith("http://") && !normalized.toLowerCase().startsWith("https://")) {
			normalized = "https://" + normalized;
		}

		normalized = normalized.replaceAll("(?<!:)/{2,}", "/");

		if (normalized.matches("https?://[^/]+/+$")) {
			normalized = normalized.replaceAll("/+$", "");
		}

		return normalized;
	}

}
