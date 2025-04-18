package org.springframework.ai.model.watsonxai.autoconfigure;

import java.util.List;

import org.springframework.ai.watsonx.WatsonxAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(WatsonxAiChatProperties.CONFIG_PREFIX)
public class WatsonxAiChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.watsonx.ai.chat";

	@NestedConfigurationProperty
	private WatsonxAiChatOptions options = WatsonxAiChatOptions.builder()
		.model("google/flan-ul2")
		.temperature(0.7)
		.topP(1.0)
		.topK(50)
		.decodingMethod("greedy")
		.maxNewTokens(20)
		.minNewTokens(0)
		.repetitionPenalty(1.0)
		.stopSequences(List.of())
		.build();

	public WatsonxAiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(WatsonxAiChatOptions options) {
		this.options = options;
	}

}
