package org.springframework.ai.observation.conventions;

public enum AiObservationAttributes {

// @formatter:off

	AI_OPERATION_TYPE("gen_ai.operation.name"),

	AI_PROVIDER("gen_ai.system"),

	REQUEST_MODEL("gen_ai.request.model"),

	REQUEST_FREQUENCY_PENALTY("gen_ai.request.frequency_penalty"),

	REQUEST_MAX_TOKENS("gen_ai.request.max_tokens"),

	REQUEST_PRESENCE_PENALTY("gen_ai.request.presence_penalty"),

	REQUEST_STOP_SEQUENCES("gen_ai.request.stop_sequences"),

	REQUEST_TEMPERATURE("gen_ai.request.temperature"),

	REQUEST_TOP_K("gen_ai.request.top_k"),

	REQUEST_TOP_P("gen_ai.request.top_p"),

	REQUEST_EMBEDDING_DIMENSIONS("gen_ai.request.embedding.dimensions"),

	REQUEST_IMAGE_RESPONSE_FORMAT("gen_ai.request.image.response_format"),

	REQUEST_IMAGE_SIZE("gen_ai.request.image.size"),

	REQUEST_IMAGE_STYLE("gen_ai.request.image.style"),

	RESPONSE_FINISH_REASONS("gen_ai.response.finish_reasons"),

	RESPONSE_ID("gen_ai.response.id"),

	RESPONSE_MODEL("gen_ai.response.model"),

	USAGE_INPUT_TOKENS("gen_ai.usage.input_tokens"),

	USAGE_OUTPUT_TOKENS("gen_ai.usage.output_tokens"),

	USAGE_TOTAL_TOKENS("gen_ai.usage.total_tokens"),

	PROMPT("gen_ai.prompt"),

	COMPLETION("gen_ai.completion");

	private final String value;

	AiObservationAttributes(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
