package org.springframework.ai.observation.conventions;

public enum AiProvider {

	// @formatter:off

	ANTHROPIC("anthropic"),

	AZURE_OPENAI("azure-openai"),

	BEDROCK_CONVERSE("bedrock_converse"),

	MISTRAL_AI("mistral_ai"),

	OCI_GENAI("oci_genai"),

	OLLAMA("ollama"),

	OPENAI("openai"),

	MINIMAX("minimax"),

	ZHIPUAI("zhipuai"),

	SPRING_AI("spring_ai"),

	VERTEX_AI("vertex_ai"),

	ONNX("onnx");

	private final String value;

	AiProvider(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	// @formatter:on

}
