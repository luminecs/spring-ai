package org.springframework.ai.model.oci.genai.autoconfigure;

public enum ServingMode {

	ON_DEMAND("on-demand"), DEDICATED("dedicated");

	private final String mode;

	ServingMode(String mode) {
		this.mode = mode;
	}

	public String getMode() {
		return this.mode;
	}

}
