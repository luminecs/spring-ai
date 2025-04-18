package org.springframework.ai.model;

import org.springframework.util.Assert;

public record SimpleApiKey(String value) implements ApiKey {

	public SimpleApiKey(String value) {
		Assert.notNull(value, "API key value must not be null or empty");
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value();
	}

	@Override
	public String toString() {
		return "SimpleApiKey{value='***'}";
	}
}
