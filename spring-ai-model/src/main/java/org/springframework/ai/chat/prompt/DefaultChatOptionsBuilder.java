package org.springframework.ai.chat.prompt;

import java.util.List;

public class DefaultChatOptionsBuilder implements ChatOptions.Builder {

	protected DefaultChatOptions options;

	public DefaultChatOptionsBuilder() {
		this.options = new DefaultChatOptions();
	}

	protected DefaultChatOptionsBuilder(DefaultChatOptions options) {
		this.options = options;
	}

	public DefaultChatOptionsBuilder model(String model) {
		this.options.setModel(model);
		return this;
	}

	public DefaultChatOptionsBuilder frequencyPenalty(Double frequencyPenalty) {
		this.options.setFrequencyPenalty(frequencyPenalty);
		return this;
	}

	public DefaultChatOptionsBuilder maxTokens(Integer maxTokens) {
		this.options.setMaxTokens(maxTokens);
		return this;
	}

	public DefaultChatOptionsBuilder presencePenalty(Double presencePenalty) {
		this.options.setPresencePenalty(presencePenalty);
		return this;
	}

	public DefaultChatOptionsBuilder stopSequences(List<String> stop) {
		this.options.setStopSequences(stop);
		return this;
	}

	public DefaultChatOptionsBuilder temperature(Double temperature) {
		this.options.setTemperature(temperature);
		return this;
	}

	public DefaultChatOptionsBuilder topK(Integer topK) {
		this.options.setTopK(topK);
		return this;
	}

	public DefaultChatOptionsBuilder topP(Double topP) {
		this.options.setTopP(topP);
		return this;
	}

	public ChatOptions build() {
		return this.options.copy();
	}

}
