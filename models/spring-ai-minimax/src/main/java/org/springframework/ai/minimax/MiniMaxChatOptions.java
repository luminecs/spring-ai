package org.springframework.ai.minimax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class MiniMaxChatOptions implements ToolCallingChatOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("frequency_penalty") Double frequencyPenalty;

	private @JsonProperty("max_tokens") Integer maxTokens;

	private @JsonProperty("n") Integer n;

	private @JsonProperty("presence_penalty") Double presencePenalty;

	private @JsonProperty("response_format") MiniMaxApi.ChatCompletionRequest.ResponseFormat responseFormat;

	private @JsonProperty("seed") Integer seed;

	private @JsonProperty("stop") List<String> stop;

	private @JsonProperty("temperature") Double temperature;

	private @JsonProperty("top_p") Double topP;

	private @JsonProperty("mask_sensitive_info") Boolean maskSensitiveInfo;

	private @JsonProperty("tools") List<MiniMaxApi.FunctionTool> tools;

	private @JsonProperty("tool_choice") String toolChoice;

	@JsonIgnore
	private List<ToolCallback> toolCallbacks = new ArrayList<>();

	@JsonIgnore
	private Set<String> toolNames = new HashSet<>();

	@JsonIgnore
	private Map<String, Object> toolContext = new HashMap<>();

	@JsonIgnore
	private Boolean internalToolExecutionEnabled;

	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	public static MiniMaxChatOptions fromOptions(MiniMaxChatOptions fromOptions) {
		return builder().model(fromOptions.getModel())
			.frequencyPenalty(fromOptions.getFrequencyPenalty())
			.maxTokens(fromOptions.getMaxTokens())
			.N(fromOptions.getN())
			.presencePenalty(fromOptions.getPresencePenalty())
			.responseFormat(fromOptions.getResponseFormat())
			.seed(fromOptions.getSeed())
			.stop(fromOptions.getStop())
			.temperature(fromOptions.getTemperature())
			.topP(fromOptions.getTopP())
			.maskSensitiveInfo(fromOptions.getMaskSensitiveInfo())
			.tools(fromOptions.getTools())
			.toolChoice(fromOptions.getToolChoice())
			.toolCallbacks(fromOptions.getToolCallbacks())
			.toolNames(fromOptions.getToolNames())
			.internalToolExecutionEnabled(fromOptions.getInternalToolExecutionEnabled())
			.toolContext(fromOptions.getToolContext())
			.build();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Double getFrequencyPenalty() {
		return this.frequencyPenalty;
	}

	public void setFrequencyPenalty(Double frequencyPenalty) {
		this.frequencyPenalty = frequencyPenalty;
	}

	@Override
	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	@Override
	public Double getPresencePenalty() {
		return this.presencePenalty;
	}

	public void setPresencePenalty(Double presencePenalty) {
		this.presencePenalty = presencePenalty;
	}

	public MiniMaxApi.ChatCompletionRequest.ResponseFormat getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(MiniMaxApi.ChatCompletionRequest.ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public Integer getSeed() {
		return this.seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	@Override
	@JsonIgnore
	public List<String> getStopSequences() {
		return getStop();
	}

	@JsonIgnore
	public void setStopSequences(List<String> stopSequences) {
		setStop(stopSequences);
	}

	public List<String> getStop() {
		return this.stop;
	}

	public void setStop(List<String> stop) {
		this.stop = stop;
	}

	@Override
	public Double getTemperature() {
		return this.temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	@Override
	public Double getTopP() {
		return this.topP;
	}

	public void setTopP(Double topP) {
		this.topP = topP;
	}

	public Boolean getMaskSensitiveInfo() {
		return this.maskSensitiveInfo;
	}

	public void setMaskSensitiveInfo(Boolean maskSensitiveInfo) {
		this.maskSensitiveInfo = maskSensitiveInfo;
	}

	public List<MiniMaxApi.FunctionTool> getTools() {
		return this.tools;
	}

	public void setTools(List<MiniMaxApi.FunctionTool> tools) {
		this.tools = tools;
	}

	public String getToolChoice() {
		return this.toolChoice;
	}

	public void setToolChoice(String toolChoice) {
		this.toolChoice = toolChoice;
	}

	@Override
	@JsonIgnore
	public Integer getTopK() {
		return null;
	}

	@Override
	@JsonIgnore
	public List<ToolCallback> getToolCallbacks() {
		return this.toolCallbacks;
	}

	@Override
	@JsonIgnore
	public void setToolCallbacks(List<ToolCallback> toolCallbacks) {
		Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
		this.toolCallbacks = toolCallbacks;
	}

	@Override
	@JsonIgnore
	public Set<String> getToolNames() {
		return this.toolNames;
	}

	@Override
	@JsonIgnore
	public void setToolNames(Set<String> toolNames) {
		Assert.notNull(toolNames, "toolNames cannot be null");
		Assert.noNullElements(toolNames, "toolNames cannot contain null elements");
		toolNames.forEach(tool -> Assert.hasText(tool, "toolNames cannot contain empty elements"));
		this.toolNames = toolNames;
	}

	@Override
	@Nullable
	@JsonIgnore
	public Boolean getInternalToolExecutionEnabled() {
		return this.internalToolExecutionEnabled;
	}

	@Override
	@JsonIgnore
	public void setInternalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled) {
		this.internalToolExecutionEnabled = internalToolExecutionEnabled;
	}

	@Override
	public Map<String, Object> getToolContext() {
		return this.toolContext;
	}

	@Override
	public void setToolContext(Map<String, Object> toolContext) {
		this.toolContext = toolContext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.model == null) ? 0 : this.model.hashCode());
		result = prime * result + ((this.frequencyPenalty == null) ? 0 : this.frequencyPenalty.hashCode());
		result = prime * result + ((this.maxTokens == null) ? 0 : this.maxTokens.hashCode());
		result = prime * result + ((this.n == null) ? 0 : this.n.hashCode());
		result = prime * result + ((this.presencePenalty == null) ? 0 : this.presencePenalty.hashCode());
		result = prime * result + ((this.responseFormat == null) ? 0 : this.responseFormat.hashCode());
		result = prime * result + ((this.seed == null) ? 0 : this.seed.hashCode());
		result = prime * result + ((this.stop == null) ? 0 : this.stop.hashCode());
		result = prime * result + ((this.temperature == null) ? 0 : this.temperature.hashCode());
		result = prime * result + ((this.topP == null) ? 0 : this.topP.hashCode());
		result = prime * result + ((this.maskSensitiveInfo == null) ? 0 : this.maskSensitiveInfo.hashCode());
		result = prime * result + ((this.tools == null) ? 0 : this.tools.hashCode());
		result = prime * result + ((this.toolChoice == null) ? 0 : this.toolChoice.hashCode());
		result = prime * result + ((this.toolCallbacks == null) ? 0 : this.toolCallbacks.hashCode());
		result = prime * result + ((this.toolNames == null) ? 0 : this.toolNames.hashCode());
		result = prime * result
				+ ((this.internalToolExecutionEnabled == null) ? 0 : this.internalToolExecutionEnabled.hashCode());
		result = prime * result + ((this.toolContext == null) ? 0 : this.toolContext.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MiniMaxChatOptions other = (MiniMaxChatOptions) obj;
		if (this.model == null) {
			if (other.model != null) {
				return false;
			}
		}
		else if (!this.model.equals(other.model)) {
			return false;
		}
		if (this.frequencyPenalty == null) {
			if (other.frequencyPenalty != null) {
				return false;
			}
		}
		else if (!this.frequencyPenalty.equals(other.frequencyPenalty)) {
			return false;
		}
		if (this.maxTokens == null) {
			if (other.maxTokens != null) {
				return false;
			}
		}
		else if (!this.maxTokens.equals(other.maxTokens)) {
			return false;
		}
		if (this.n == null) {
			if (other.n != null) {
				return false;
			}
		}
		else if (!this.n.equals(other.n)) {
			return false;
		}
		if (this.presencePenalty == null) {
			if (other.presencePenalty != null) {
				return false;
			}
		}
		else if (!this.presencePenalty.equals(other.presencePenalty)) {
			return false;
		}
		if (this.responseFormat == null) {
			if (other.responseFormat != null) {
				return false;
			}
		}
		else if (!this.responseFormat.equals(other.responseFormat)) {
			return false;
		}
		if (this.seed == null) {
			if (other.seed != null) {
				return false;
			}
		}
		else if (!this.seed.equals(other.seed)) {
			return false;
		}
		if (this.stop == null) {
			if (other.stop != null) {
				return false;
			}
		}
		else if (!this.stop.equals(other.stop)) {
			return false;
		}
		if (this.temperature == null) {
			if (other.temperature != null) {
				return false;
			}
		}
		else if (!this.temperature.equals(other.temperature)) {
			return false;
		}
		if (this.topP == null) {
			if (other.topP != null) {
				return false;
			}
		}
		else if (!this.topP.equals(other.topP)) {
			return false;
		}
		if (this.maskSensitiveInfo == null) {
			if (other.maskSensitiveInfo != null) {
				return false;
			}
		}
		else if (!this.maskSensitiveInfo.equals(other.maskSensitiveInfo)) {
			return false;
		}
		if (this.tools == null) {
			if (other.tools != null) {
				return false;
			}
		}
		else if (!this.tools.equals(other.tools)) {
			return false;
		}
		if (this.toolChoice == null) {
			if (other.toolChoice != null) {
				return false;
			}
		}
		else if (!this.toolChoice.equals(other.toolChoice)) {
			return false;
		}
		if (this.internalToolExecutionEnabled == null) {
			if (other.internalToolExecutionEnabled != null) {
				return false;
			}
		}
		else if (!this.internalToolExecutionEnabled.equals(other.internalToolExecutionEnabled)) {
			return false;
		}

		if (this.toolNames == null) {
			if (other.toolNames != null) {
				return false;
			}
		}
		else if (!this.toolNames.equals(other.toolNames)) {
			return false;
		}

		if (this.toolCallbacks == null) {
			if (other.toolCallbacks != null) {
				return false;
			}
		}
		else if (!this.toolCallbacks.equals(other.toolCallbacks)) {
			return false;
		}

		if (this.toolContext == null) {
			if (other.toolContext != null) {
				return false;
			}
		}
		else if (!this.toolContext.equals(other.toolContext)) {
			return false;
		}

		return true;
	}

	@Override
	public MiniMaxChatOptions copy() {
		return fromOptions(this);
	}

	public static class Builder {

		protected MiniMaxChatOptions options;

		public Builder() {
			this.options = new MiniMaxChatOptions();
		}

		public Builder(MiniMaxChatOptions options) {
			this.options = options;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder frequencyPenalty(Double frequencyPenalty) {
			this.options.frequencyPenalty = frequencyPenalty;
			return this;
		}

		public Builder maxTokens(Integer maxTokens) {
			this.options.maxTokens = maxTokens;
			return this;
		}

		public Builder N(Integer n) {
			this.options.n = n;
			return this;
		}

		public Builder presencePenalty(Double presencePenalty) {
			this.options.presencePenalty = presencePenalty;
			return this;
		}

		public Builder responseFormat(MiniMaxApi.ChatCompletionRequest.ResponseFormat responseFormat) {
			this.options.responseFormat = responseFormat;
			return this;
		}

		public Builder seed(Integer seed) {
			this.options.seed = seed;
			return this;
		}

		public Builder stop(List<String> stop) {
			this.options.stop = stop;
			return this;
		}

		public Builder temperature(Double temperature) {
			this.options.temperature = temperature;
			return this;
		}

		public Builder topP(Double topP) {
			this.options.topP = topP;
			return this;
		}

		public Builder maskSensitiveInfo(Boolean maskSensitiveInfo) {
			this.options.maskSensitiveInfo = maskSensitiveInfo;
			return this;
		}

		public Builder tools(List<MiniMaxApi.FunctionTool> tools) {
			this.options.tools = tools;
			return this;
		}

		public Builder toolChoice(String toolChoice) {
			this.options.toolChoice = toolChoice;
			return this;
		}

		public Builder toolCallbacks(List<ToolCallback> toolCallbacks) {
			this.options.setToolCallbacks(toolCallbacks);
			return this;
		}

		public Builder toolCallbacks(ToolCallback... toolCallbacks) {
			Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
			this.options.toolCallbacks.addAll(Arrays.asList(toolCallbacks));
			return this;
		}

		public Builder toolNames(Set<String> toolNames) {
			Assert.notNull(toolNames, "toolNames cannot be null");
			this.options.setToolNames(toolNames);
			return this;
		}

		public Builder toolNames(String... toolNames) {
			Assert.notNull(toolNames, "toolNames cannot be null");
			this.options.toolNames.addAll(Set.of(toolNames));
			return this;
		}

		public Builder internalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled) {
			this.options.setInternalToolExecutionEnabled(internalToolExecutionEnabled);
			return this;
		}

		public Builder toolContext(Map<String, Object> toolContext) {
			if (this.options.toolContext == null) {
				this.options.toolContext = toolContext;
			}
			else {
				this.options.toolContext.putAll(toolContext);
			}
			return this;
		}

		public MiniMaxChatOptions build() {
			return this.options;
		}

	}

}
