package org.springframework.ai.openai.metadata.support;

public enum OpenAiApiResponseHeaders {

	REQUESTS_LIMIT_HEADER("x-ratelimit-limit-requests", "Total number of requests allowed within timeframe."),
	REQUESTS_REMAINING_HEADER("x-ratelimit-remaining-requests", "Remaining number of requests available in timeframe."),
	REQUESTS_RESET_HEADER("x-ratelimit-reset-requests", "Duration of time until the number of requests reset."),
	TOKENS_RESET_HEADER("x-ratelimit-reset-tokens", "Total number of tokens allowed within timeframe."),
	TOKENS_LIMIT_HEADER("x-ratelimit-limit-tokens", "Remaining number of tokens available in timeframe."),
	TOKENS_REMAINING_HEADER("x-ratelimit-remaining-tokens", "Duration of time until the number of tokens reset.");

	private final String headerName;

	private final String description;

	OpenAiApiResponseHeaders(String headerName, String description) {
		this.headerName = headerName;
		this.description = description;
	}

	public String getName() {
		return this.headerName;
	}

	public String getDescription() {
		return this.description;
	}

}
