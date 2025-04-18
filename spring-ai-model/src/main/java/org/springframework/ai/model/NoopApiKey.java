package org.springframework.ai.model;

public class NoopApiKey implements ApiKey {

	@Override
	public String getValue() {
		return "";
	}

}
