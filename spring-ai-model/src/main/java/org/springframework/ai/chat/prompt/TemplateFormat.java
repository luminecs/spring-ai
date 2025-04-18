package org.springframework.ai.chat.prompt;

public enum TemplateFormat {

	ST("ST");

	private final String value;

	TemplateFormat(String value) {
		this.value = value;
	}

	public static TemplateFormat fromValue(String value) {
		for (TemplateFormat templateFormat : TemplateFormat.values()) {
			if (templateFormat.getValue().equals(value)) {
				return templateFormat;
			}
		}
		throw new IllegalArgumentException("Invalid TemplateFormat value: " + value);
	}

	public String getValue() {
		return this.value;
	}

}
