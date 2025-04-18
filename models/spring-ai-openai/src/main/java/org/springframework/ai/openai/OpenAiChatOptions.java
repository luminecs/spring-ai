package org.springframework.ai.openai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.StreamOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.ToolChoiceBuilder;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class OpenAiChatOptions implements ToolCallingChatOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("frequency_penalty") Double frequencyPenalty;

	private @JsonProperty("logit_bias") Map<String, Integer> logitBias;

	private @JsonProperty("logprobs") Boolean logprobs;

	private @JsonProperty("top_logprobs") Integer topLogprobs;

	private @JsonProperty("max_tokens") Integer maxTokens;

	private @JsonProperty("max_completion_tokens") Integer maxCompletionTokens;

	private @JsonProperty("n") Integer n;

	private @JsonProperty("modalities") List<String> outputModalities;

	private @JsonProperty("audio") AudioParameters outputAudio;

	private @JsonProperty("presence_penalty") Double presencePenalty;

	private @JsonProperty("response_format") ResponseFormat responseFormat;

	private @JsonProperty("stream_options") StreamOptions streamOptions;

	private @JsonProperty("seed") Integer seed;

	private @JsonProperty("stop") List<String> stop;

	private @JsonProperty("temperature") Double temperature;

	private @JsonProperty("top_p") Double topP;

	private @JsonProperty("tools") List<OpenAiApi.FunctionTool> tools;

	private @JsonProperty("tool_choice") Object toolChoice;

	private @JsonProperty("user") String user;

	private @JsonProperty("parallel_tool_calls") Boolean parallelToolCalls;

	private @JsonProperty("store") Boolean store;

	private @JsonProperty("metadata") Map<String, String> metadata;

	private @JsonProperty("reasoning_effort") String reasoningEffort;

	@JsonIgnore
	private List<FunctionCallback> toolCallbacks = new ArrayList<>();

	@JsonIgnore
	private Set<String> toolNames = new HashSet<>();

	@JsonIgnore
	private Boolean internalToolExecutionEnabled;

	@JsonIgnore
	private Map<String, String> httpHeaders = new HashMap<>();

	@JsonIgnore
	private Map<String, Object> toolContext = new HashMap<>();

	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	public static OpenAiChatOptions fromOptions(OpenAiChatOptions fromOptions) {
		return OpenAiChatOptions.builder()
			.model(fromOptions.getModel())
			.frequencyPenalty(fromOptions.getFrequencyPenalty())
			.logitBias(fromOptions.getLogitBias())
			.logprobs(fromOptions.getLogprobs())
			.topLogprobs(fromOptions.getTopLogprobs())
			.maxTokens(fromOptions.getMaxTokens())
			.maxCompletionTokens(fromOptions.getMaxCompletionTokens())
			.N(fromOptions.getN())
			.outputModalities(fromOptions.getOutputModalities() != null
					? new ArrayList<>(fromOptions.getOutputModalities()) : null)
			.outputAudio(fromOptions.getOutputAudio())
			.presencePenalty(fromOptions.getPresencePenalty())
			.responseFormat(fromOptions.getResponseFormat())
			.streamUsage(fromOptions.getStreamUsage())
			.seed(fromOptions.getSeed())
			.stop(fromOptions.getStop() != null ? new ArrayList<>(fromOptions.getStop()) : null)
			.temperature(fromOptions.getTemperature())
			.topP(fromOptions.getTopP())
			.tools(fromOptions.getTools())
			.toolChoice(fromOptions.getToolChoice())
			.user(fromOptions.getUser())
			.parallelToolCalls(fromOptions.getParallelToolCalls())
			.toolCallbacks(
					fromOptions.getToolCallbacks() != null ? new ArrayList<>(fromOptions.getToolCallbacks()) : null)
			.toolNames(fromOptions.getToolNames() != null ? new HashSet<>(fromOptions.getToolNames()) : null)
			.httpHeaders(fromOptions.getHttpHeaders() != null ? new HashMap<>(fromOptions.getHttpHeaders()) : null)
			.internalToolExecutionEnabled(fromOptions.getInternalToolExecutionEnabled())
			.toolContext(fromOptions.getToolContext() != null ? new HashMap<>(fromOptions.getToolContext()) : null)
			.store(fromOptions.getStore())
			.metadata(fromOptions.getMetadata())
			.reasoningEffort(fromOptions.getReasoningEffort())
			.build();
	}

	public Boolean getStreamUsage() {
		return this.streamOptions != null;
	}

	public void setStreamUsage(Boolean enableStreamUsage) {
		this.streamOptions = (enableStreamUsage) ? StreamOptions.INCLUDE_USAGE : null;
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

	public Map<String, Integer> getLogitBias() {
		return this.logitBias;
	}

	public void setLogitBias(Map<String, Integer> logitBias) {
		this.logitBias = logitBias;
	}

	public Boolean getLogprobs() {
		return this.logprobs;
	}

	public void setLogprobs(Boolean logprobs) {
		this.logprobs = logprobs;
	}

	public Integer getTopLogprobs() {
		return this.topLogprobs;
	}

	public void setTopLogprobs(Integer topLogprobs) {
		this.topLogprobs = topLogprobs;
	}

	@Override
	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public Integer getMaxCompletionTokens() {
		return this.maxCompletionTokens;
	}

	public void setMaxCompletionTokens(Integer maxCompletionTokens) {
		this.maxCompletionTokens = maxCompletionTokens;
	}

	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public List<String> getOutputModalities() {
		return this.outputModalities;
	}

	public void setOutputModalities(List<String> modalities) {
		this.outputModalities = modalities;
	}

	public AudioParameters getOutputAudio() {
		return this.outputAudio;
	}

	public void setOutputAudio(AudioParameters audio) {
		this.outputAudio = audio;
	}

	@Override
	public Double getPresencePenalty() {
		return this.presencePenalty;
	}

	public void setPresencePenalty(Double presencePenalty) {
		this.presencePenalty = presencePenalty;
	}

	public ResponseFormat getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public StreamOptions getStreamOptions() {
		return this.streamOptions;
	}

	public void setStreamOptions(StreamOptions streamOptions) {
		this.streamOptions = streamOptions;
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

	public List<OpenAiApi.FunctionTool> getTools() {
		return this.tools;
	}

	public void setTools(List<OpenAiApi.FunctionTool> tools) {
		this.tools = tools;
	}

	public Object getToolChoice() {
		return this.toolChoice;
	}

	public void setToolChoice(Object toolChoice) {
		this.toolChoice = toolChoice;
	}

	@Override
	@Deprecated
	@JsonIgnore
	public Boolean getProxyToolCalls() {
		return this.getToolExecutionEnabled() != null ? !this.internalToolExecutionEnabled : null;
	}

	private Boolean getToolExecutionEnabled() {
		return this.internalToolExecutionEnabled;
	}

	@Deprecated
	@JsonIgnore
	public void setProxyToolCalls(Boolean proxyToolCalls) {
		this.internalToolExecutionEnabled = proxyToolCalls != null ? !proxyToolCalls : null;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Boolean getParallelToolCalls() {
		return this.parallelToolCalls;
	}

	public void setParallelToolCalls(Boolean parallelToolCalls) {
		this.parallelToolCalls = parallelToolCalls;
	}

	@Override
	@JsonIgnore
	public List<FunctionCallback> getToolCallbacks() {
		return this.toolCallbacks;
	}

	@Override
	@JsonIgnore
	public void setToolCallbacks(List<FunctionCallback> toolCallbacks) {
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
	@Deprecated
	@JsonIgnore
	public List<FunctionCallback> getFunctionCallbacks() {
		return this.getToolCallbacks();
	}

	@Override
	@Deprecated
	@JsonIgnore
	public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
		this.setToolCallbacks(functionCallbacks);
	}

	@Override
	@Deprecated
	@JsonIgnore
	public Set<String> getFunctions() {
		return this.getToolNames();
	}

	@Override
	@Deprecated
	@JsonIgnore
	public void setFunctions(Set<String> functionNames) {
		this.setToolNames(functionNames);
	}

	public Map<String, String> getHttpHeaders() {
		return this.httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	@Override
	@JsonIgnore
	public Integer getTopK() {
		return null;
	}

	@Override
	@JsonIgnore
	public Map<String, Object> getToolContext() {
		return this.toolContext;
	}

	@Override
	@JsonIgnore
	public void setToolContext(Map<String, Object> toolContext) {
		this.toolContext = toolContext;
	}

	public Boolean getStore() {
		return this.store;
	}

	public void setStore(Boolean store) {
		this.store = store;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getReasoningEffort() {
		return this.reasoningEffort;
	}

	public void setReasoningEffort(String reasoningEffort) {
		this.reasoningEffort = reasoningEffort;
	}

	@Override
	public OpenAiChatOptions copy() {
		return OpenAiChatOptions.fromOptions(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.model, this.frequencyPenalty, this.logitBias, this.logprobs, this.topLogprobs,
				this.maxTokens, this.maxCompletionTokens, this.n, this.presencePenalty, this.responseFormat,
				this.streamOptions, this.seed, this.stop, this.temperature, this.topP, this.tools, this.toolChoice,
				this.user, this.parallelToolCalls, this.toolCallbacks, this.toolNames, this.httpHeaders,
				this.internalToolExecutionEnabled, this.toolContext, this.outputModalities, this.outputAudio,
				this.store, this.metadata, this.reasoningEffort);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OpenAiChatOptions other = (OpenAiChatOptions) o;
		return Objects.equals(this.model, other.model) && Objects.equals(this.frequencyPenalty, other.frequencyPenalty)
				&& Objects.equals(this.logitBias, other.logitBias) && Objects.equals(this.logprobs, other.logprobs)
				&& Objects.equals(this.topLogprobs, other.topLogprobs)
				&& Objects.equals(this.maxTokens, other.maxTokens)
				&& Objects.equals(this.maxCompletionTokens, other.maxCompletionTokens)
				&& Objects.equals(this.n, other.n) && Objects.equals(this.presencePenalty, other.presencePenalty)
				&& Objects.equals(this.responseFormat, other.responseFormat)
				&& Objects.equals(this.streamOptions, other.streamOptions) && Objects.equals(this.seed, other.seed)
				&& Objects.equals(this.stop, other.stop) && Objects.equals(this.temperature, other.temperature)
				&& Objects.equals(this.topP, other.topP) && Objects.equals(this.tools, other.tools)
				&& Objects.equals(this.toolChoice, other.toolChoice) && Objects.equals(this.user, other.user)
				&& Objects.equals(this.parallelToolCalls, other.parallelToolCalls)
				&& Objects.equals(this.toolCallbacks, other.toolCallbacks)
				&& Objects.equals(this.toolNames, other.toolNames)
				&& Objects.equals(this.httpHeaders, other.httpHeaders)
				&& Objects.equals(this.toolContext, other.toolContext)
				&& Objects.equals(this.internalToolExecutionEnabled, other.internalToolExecutionEnabled)
				&& Objects.equals(this.outputModalities, other.outputModalities)
				&& Objects.equals(this.outputAudio, other.outputAudio) && Objects.equals(this.store, other.store)
				&& Objects.equals(this.metadata, other.metadata)
				&& Objects.equals(this.reasoningEffort, other.reasoningEffort);
	}

	@Override
	public String toString() {
		return "OpenAiChatOptions: " + ModelOptionsUtils.toJsonString(this);
	}

	public static class Builder {

		protected OpenAiChatOptions options;

		public Builder() {
			this.options = new OpenAiChatOptions();
		}

		public Builder(OpenAiChatOptions options) {
			this.options = options;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder model(OpenAiApi.ChatModel openAiChatModel) {
			this.options.model = openAiChatModel.getName();
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

		public Builder logprobs(Boolean logprobs) {
			this.options.logprobs = logprobs;
			return this;
		}

		public Builder topLogprobs(Integer topLogprobs) {
			this.options.topLogprobs = topLogprobs;
			return this;
		}

		public Builder maxTokens(Integer maxTokens) {
			this.options.maxTokens = maxTokens;
			return this;
		}

		public Builder maxCompletionTokens(Integer maxCompletionTokens) {
			this.options.maxCompletionTokens = maxCompletionTokens;
			return this;
		}

		public Builder N(Integer n) {
			this.options.n = n;
			return this;
		}

		public Builder outputModalities(List<String> modalities) {
			this.options.outputModalities = modalities;
			return this;
		}

		public Builder outputAudio(AudioParameters audio) {
			this.options.outputAudio = audio;
			return this;
		}

		public Builder presencePenalty(Double presencePenalty) {
			this.options.presencePenalty = presencePenalty;
			return this;
		}

		public Builder responseFormat(ResponseFormat responseFormat) {
			this.options.responseFormat = responseFormat;
			return this;
		}

		public Builder streamUsage(boolean enableStreamUsage) {
			this.options.streamOptions = (enableStreamUsage) ? StreamOptions.INCLUDE_USAGE : null;
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

		public Builder tools(List<OpenAiApi.FunctionTool> tools) {
			this.options.tools = tools;
			return this;
		}

		public Builder toolChoice(Object toolChoice) {
			this.options.toolChoice = toolChoice;
			return this;
		}

		public Builder user(String user) {
			this.options.user = user;
			return this;
		}

		public Builder parallelToolCalls(Boolean parallelToolCalls) {
			this.options.parallelToolCalls = parallelToolCalls;
			return this;
		}

		public Builder toolCallbacks(List<FunctionCallback> toolCallbacks) {
			this.options.setToolCallbacks(toolCallbacks);
			return this;
		}

		public Builder toolCallbacks(FunctionCallback... toolCallbacks) {
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

		@Deprecated
		public Builder functionCallbacks(List<FunctionCallback> functionCallbacks) {
			return toolCallbacks(functionCallbacks);
		}

		@Deprecated
		public Builder functions(Set<String> functionNames) {
			return toolNames(functionNames);
		}

		@Deprecated
		public Builder function(String functionName) {
			return toolNames(functionName);
		}

		@Deprecated
		public Builder proxyToolCalls(Boolean proxyToolCalls) {
			if (proxyToolCalls != null) {
				this.options.setInternalToolExecutionEnabled(!proxyToolCalls);
			}
			return this;
		}

		public Builder httpHeaders(Map<String, String> httpHeaders) {
			this.options.httpHeaders = httpHeaders;
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

		public Builder store(Boolean store) {
			this.options.store = store;
			return this;
		}

		public Builder metadata(Map<String, String> metadata) {
			this.options.metadata = metadata;
			return this;
		}

		public Builder reasoningEffort(String reasoningEffort) {
			this.options.reasoningEffort = reasoningEffort;
			return this;
		}

		public OpenAiChatOptions build() {
			return this.options;
		}

	}

}
