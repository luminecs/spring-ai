package org.springframework.ai.azure.openai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.azure.ai.openai.models.AzureChatEnhancementConfiguration;
import com.azure.ai.openai.models.ChatCompletionStreamOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class AzureOpenAiChatOptions implements ToolCallingChatOptions {

	@JsonProperty("max_tokens")
	private Integer maxTokens;

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("top_p")
	private Double topP;

	@JsonProperty("logit_bias")
	private Map<String, Integer> logitBias;

	@JsonProperty("user")
	private String user;

	@JsonProperty("n")
	private Integer n;

	@JsonProperty("stop")
	private List<String> stop;

	@JsonProperty("presence_penalty")
	private Double presencePenalty;

	@JsonProperty("frequency_penalty")
	private Double frequencyPenalty;

	@JsonProperty("deployment_name")
	private String deploymentName;

	@JsonProperty("response_format")
	private AzureOpenAiResponseFormat responseFormat;

	@JsonProperty("seed")
	private Long seed;

	@JsonProperty("log_probs")
	private Boolean logprobs;

	@JsonProperty("top_log_probs")
	private Integer topLogProbs;

	@JsonIgnore
	private AzureChatEnhancementConfiguration enhancements;

	@JsonProperty("stream_options")
	private ChatCompletionStreamOptions streamOptions;

	@JsonIgnore
	private Map<String, Object> toolContext = new HashMap<>();

	@JsonIgnore
	private List<ToolCallback> toolCallbacks = new ArrayList<>();

	@JsonIgnore
	private Set<String> toolNames = new HashSet<>();

	@JsonIgnore
	private Boolean internalToolExecutionEnabled;

	@JsonIgnore
	private Boolean enableStreamUsage;

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

	public static Builder builder() {
		return new Builder();
	}

	public static AzureOpenAiChatOptions fromOptions(AzureOpenAiChatOptions fromOptions) {
		return builder().deploymentName(fromOptions.getDeploymentName())
			.frequencyPenalty(fromOptions.getFrequencyPenalty() != null ? fromOptions.getFrequencyPenalty() : null)
			.logitBias(fromOptions.getLogitBias())
			.maxTokens(fromOptions.getMaxTokens())
			.N(fromOptions.getN())
			.presencePenalty(fromOptions.getPresencePenalty() != null ? fromOptions.getPresencePenalty() : null)
			.stop(fromOptions.getStop() != null ? new ArrayList<>(fromOptions.getStop()) : null)
			.temperature(fromOptions.getTemperature())
			.topP(fromOptions.getTopP())
			.user(fromOptions.getUser())
			.toolCallbacks(
					fromOptions.getToolCallbacks() != null ? new ArrayList<>(fromOptions.getToolCallbacks()) : null)
			.toolNames(fromOptions.getToolNames() != null ? new HashSet<>(fromOptions.getToolNames()) : null)
			.responseFormat(fromOptions.getResponseFormat())
			.streamUsage(fromOptions.getStreamUsage())
			.seed(fromOptions.getSeed())
			.logprobs(fromOptions.isLogprobs())
			.topLogprobs(fromOptions.getTopLogProbs())
			.enhancements(fromOptions.getEnhancements())
			.toolContext(fromOptions.getToolContext() != null ? new HashMap<>(fromOptions.getToolContext()) : null)
			.internalToolExecutionEnabled(fromOptions.getInternalToolExecutionEnabled())
			.streamOptions(fromOptions.getStreamOptions())
			.toolCallbacks(
					fromOptions.getToolCallbacks() != null ? new ArrayList<>(fromOptions.getToolCallbacks()) : null)
			.toolNames(fromOptions.getToolNames() != null ? new HashSet<>(fromOptions.getToolNames()) : null)
			.build();
	}

	@Override
	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public Map<String, Integer> getLogitBias() {
		return this.logitBias;
	}

	public void setLogitBias(Map<String, Integer> logitBias) {
		this.logitBias = logitBias;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
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
	public Double getPresencePenalty() {
		return this.presencePenalty;
	}

	public void setPresencePenalty(Double presencePenalty) {
		this.presencePenalty = presencePenalty;
	}

	@Override
	public Double getFrequencyPenalty() {
		return this.frequencyPenalty;
	}

	public void setFrequencyPenalty(Double frequencyPenalty) {
		this.frequencyPenalty = frequencyPenalty;
	}

	@Override
	@JsonIgnore
	public String getModel() {
		return getDeploymentName();
	}

	@JsonIgnore
	public void setModel(String model) {
		setDeploymentName(model);
	}

	public String getDeploymentName() {
		return this.deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
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

	public void setFunctions(Set<String> functions) {
		this.setToolNames(functions);
	}

	public AzureOpenAiResponseFormat getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(AzureOpenAiResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public Boolean getStreamUsage() {
		return this.enableStreamUsage;
	}

	public void setStreamUsage(Boolean enableStreamUsage) {
		this.enableStreamUsage = enableStreamUsage;
	}

	@Override
	@JsonIgnore
	public Integer getTopK() {
		return null;
	}

	public Long getSeed() {
		return this.seed;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
	}

	public Boolean isLogprobs() {
		return this.logprobs;
	}

	public void setLogprobs(Boolean logprobs) {
		this.logprobs = logprobs;
	}

	public Integer getTopLogProbs() {
		return this.topLogProbs;
	}

	public void setTopLogProbs(Integer topLogProbs) {
		this.topLogProbs = topLogProbs;
	}

	public AzureChatEnhancementConfiguration getEnhancements() {
		return this.enhancements;
	}

	public void setEnhancements(AzureChatEnhancementConfiguration enhancements) {
		this.enhancements = enhancements;
	}

	@Override
	public Map<String, Object> getToolContext() {
		return this.toolContext;
	}

	@Override
	public void setToolContext(Map<String, Object> toolContext) {
		this.toolContext = toolContext;
	}

	public ChatCompletionStreamOptions getStreamOptions() {
		return this.streamOptions;
	}

	public void setStreamOptions(ChatCompletionStreamOptions streamOptions) {
		this.streamOptions = streamOptions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public AzureOpenAiChatOptions copy() {
		return fromOptions(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AzureOpenAiChatOptions that)) {
			return false;
		}
		return Objects.equals(this.logitBias, that.logitBias) && Objects.equals(this.user, that.user)
				&& Objects.equals(this.n, that.n) && Objects.equals(this.stop, that.stop)
				&& Objects.equals(this.deploymentName, that.deploymentName)
				&& Objects.equals(this.responseFormat, that.responseFormat)

				&& Objects.equals(this.toolCallbacks, that.toolCallbacks)
				&& Objects.equals(this.toolNames, that.toolNames)
				&& Objects.equals(this.internalToolExecutionEnabled, that.internalToolExecutionEnabled)
				&& Objects.equals(this.logprobs, that.logprobs) && Objects.equals(this.topLogProbs, that.topLogProbs)
				&& Objects.equals(this.enhancements, that.enhancements)
				&& Objects.equals(this.streamOptions, that.streamOptions)
				&& Objects.equals(this.enableStreamUsage, that.enableStreamUsage)
				&& Objects.equals(this.toolContext, that.toolContext) && Objects.equals(this.maxTokens, that.maxTokens)
				&& Objects.equals(this.frequencyPenalty, that.frequencyPenalty)
				&& Objects.equals(this.presencePenalty, that.presencePenalty)
				&& Objects.equals(this.temperature, that.temperature) && Objects.equals(this.topP, that.topP);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.logitBias, this.user, this.n, this.stop, this.deploymentName, this.responseFormat,
				this.toolCallbacks, this.toolNames, this.internalToolExecutionEnabled, this.seed, this.logprobs,
				this.topLogProbs, this.enhancements, this.streamOptions, this.enableStreamUsage, this.toolContext,
				this.maxTokens, this.frequencyPenalty, this.presencePenalty, this.temperature, this.topP);
	}

	public static class Builder {

		protected AzureOpenAiChatOptions options;

		public Builder() {
			this.options = new AzureOpenAiChatOptions();
		}

		public Builder(AzureOpenAiChatOptions options) {
			this.options = options;
		}

		public Builder deploymentName(String deploymentName) {
			this.options.deploymentName = deploymentName;
			return this;
		}

		public Builder frequencyPenalty(Double frequencyPenalty) {
			this.options.frequencyPenalty = frequencyPenalty;
			return this;
		}

		public Builder logitBias(Map<String, Integer> logitBias) {
			this.options.logitBias = logitBias;
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

		public Builder user(String user) {
			this.options.user = user;
			return this;
		}

		public Builder responseFormat(AzureOpenAiResponseFormat responseFormat) {
			this.options.responseFormat = responseFormat;
			return this;
		}

		public Builder streamUsage(Boolean enableStreamUsage) {
			this.options.enableStreamUsage = enableStreamUsage;
			return this;
		}

		public Builder seed(Long seed) {
			this.options.seed = seed;
			return this;
		}

		public Builder logprobs(Boolean logprobs) {
			this.options.logprobs = logprobs;
			return this;
		}

		public Builder topLogprobs(Integer topLogprobs) {
			this.options.topLogProbs = topLogprobs;
			return this;
		}

		public Builder enhancements(AzureChatEnhancementConfiguration enhancements) {
			this.options.enhancements = enhancements;
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

		public Builder streamOptions(ChatCompletionStreamOptions streamOptions) {
			this.options.streamOptions = streamOptions;
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

		public AzureOpenAiChatOptions build() {
			return this.options;
		}

	}

}
