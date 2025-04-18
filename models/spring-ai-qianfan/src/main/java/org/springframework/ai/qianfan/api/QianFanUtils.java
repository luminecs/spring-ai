package org.springframework.ai.qianfan.api;

import java.util.function.Consumer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class QianFanUtils {

	public static Consumer<HttpHeaders> defaultHeaders() {
		return headers -> headers.setContentType(MediaType.APPLICATION_JSON);
	}

	private QianFanUtils() {

	}

}
