package org.springframework.ai.chat.prompt;

import java.util.List;

import org.springframework.ai.model.ModelOptions;
import org.springframework.lang.Nullable;

public interface ChatOptions extends ModelOptions {

	@Nullable
	String getModel();

	@Nullable
	Double getFrequencyPenalty();

	@Nullable
	Integer getMaxTokens();

	@Nullable
	Double getPresencePenalty();

	@Nullable
	List<String> getStopSequences();

	@Nullable
	Double getTemperature();

	@Nullable
	Integer getTopK();

	@Nullable
	Double getTopP();

	<T extends ChatOptions> T copy();

	static Builder builder() {
		return new DefaultChatOptionsBuilder();
	}

	interface Builder {

		Builder model(String model);

		Builder frequencyPenalty(Double frequencyPenalty);

		Builder maxTokens(Integer maxTokens);

		Builder presencePenalty(Double presencePenalty);

		Builder stopSequences(List<String> stopSequences);

		Builder temperature(Double temperature);

		Builder topK(Integer topK);

		Builder topP(Double topP);

		ChatOptions build();

	}

}
