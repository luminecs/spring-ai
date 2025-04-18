package org.springframework.ai.model.vertexai.autoconfigure.gemini;

import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(VertexAiGeminiChatProperties.CONFIG_PREFIX)
public class VertexAiGeminiChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vertex.ai.gemini.chat";

	public static final String DEFAULT_MODEL = VertexAiGeminiChatModel.ChatModel.GEMINI_2_0_FLASH.getValue();

	private VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
		.temperature(0.7)
		.candidateCount(1)
		.model(DEFAULT_MODEL)
		.build();

	public VertexAiGeminiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(VertexAiGeminiChatOptions options) {
		this.options = options;
	}

}
