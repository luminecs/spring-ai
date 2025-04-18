package org.springframework.ai.ollama.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class OllamaOptions implements ToolCallingChatOptions, EmbeddingOptions {

	private static final List<String> NON_SUPPORTED_FIELDS = List.of("model", "format", "keep_alive", "truncate");

	// @formatter:off

	@JsonProperty("numa")
	private Boolean useNUMA;

	@JsonProperty("num_ctx")
	private Integer numCtx;

	@JsonProperty("num_batch")
	private Integer numBatch;

	@JsonProperty("num_gpu")
	private Integer numGPU;

	@JsonProperty("main_gpu")
	private Integer mainGPU;

	@JsonProperty("low_vram")
	private Boolean lowVRAM;

	@JsonProperty("f16_kv")
	private Boolean f16KV;

	@JsonProperty("logits_all")
	private Boolean logitsAll;

	@JsonProperty("vocab_only")
	private Boolean vocabOnly;

	@JsonProperty("use_mmap")
	private Boolean useMMap;

	@JsonProperty("use_mlock")
	private Boolean useMLock;

	@JsonProperty("num_thread")
	private Integer numThread;

	@JsonProperty("num_keep")
	private Integer numKeep;

	@JsonProperty("seed")
	private Integer seed;

	@JsonProperty("num_predict")
	private Integer numPredict;

	@JsonProperty("top_k")
	private Integer topK;

	@JsonProperty("top_p")
	private Double topP;

	@JsonProperty("min_p")
	private Double minP;

	@JsonProperty("tfs_z")
	private Float tfsZ;

	@JsonProperty("typical_p")
	private Float typicalP;

	@JsonProperty("repeat_last_n")
	private Integer repeatLastN;

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("repeat_penalty")
	private Double repeatPenalty;

	@JsonProperty("presence_penalty")
	private Double presencePenalty;

	@JsonProperty("frequency_penalty")
	private Double frequencyPenalty;

	@JsonProperty("mirostat")
	private Integer mirostat;

	@JsonProperty("mirostat_tau")
	private Float mirostatTau;

	@JsonProperty("mirostat_eta")
	private Float mirostatEta;

	@JsonProperty("penalize_newline")
	private Boolean penalizeNewline;

	@JsonProperty("stop")
	private List<String> stop;

	@JsonProperty("model")
	private String model;

	@JsonProperty("format")
	private Object format;

	@JsonProperty("keep_alive")
	private String keepAlive;

	@JsonProperty("truncate")
	private Boolean truncate;

	@JsonIgnore
	private Boolean internalToolExecutionEnabled;

	@JsonIgnore
	private List<FunctionCallback> toolCallbacks = new ArrayList<>();

	@JsonIgnore
	private Set<String> toolNames = new HashSet<>();

	@JsonIgnore
	private Map<String, Object> toolContext = new HashMap<>();

	public static Builder builder() {
		return new Builder();
	}

	public static Map<String, Object> filterNonSupportedFields(Map<String, Object> options) {
		return options.entrySet().stream()
				.filter(e -> !NON_SUPPORTED_FIELDS.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public static OllamaOptions fromOptions(OllamaOptions fromOptions) {
		return builder()
				.model(fromOptions.getModel())
				.format(fromOptions.getFormat())
				.keepAlive(fromOptions.getKeepAlive())
				.truncate(fromOptions.getTruncate())
				.useNUMA(fromOptions.getUseNUMA())
				.numCtx(fromOptions.getNumCtx())
				.numBatch(fromOptions.getNumBatch())
				.numGPU(fromOptions.getNumGPU())
				.mainGPU(fromOptions.getMainGPU())
				.lowVRAM(fromOptions.getLowVRAM())
				.f16KV(fromOptions.getF16KV())
				.logitsAll(fromOptions.getLogitsAll())
				.vocabOnly(fromOptions.getVocabOnly())
				.useMMap(fromOptions.getUseMMap())
				.useMLock(fromOptions.getUseMLock())
				.numThread(fromOptions.getNumThread())
				.numKeep(fromOptions.getNumKeep())
				.seed(fromOptions.getSeed())
				.numPredict(fromOptions.getNumPredict())
				.topK(fromOptions.getTopK())
				.topP(fromOptions.getTopP())
				.minP(fromOptions.getMinP())
				.tfsZ(fromOptions.getTfsZ())
				.typicalP(fromOptions.getTypicalP())
				.repeatLastN(fromOptions.getRepeatLastN())
				.temperature(fromOptions.getTemperature())
				.repeatPenalty(fromOptions.getRepeatPenalty())
				.presencePenalty(fromOptions.getPresencePenalty())
				.frequencyPenalty(fromOptions.getFrequencyPenalty())
				.mirostat(fromOptions.getMirostat())
				.mirostatTau(fromOptions.getMirostatTau())
				.mirostatEta(fromOptions.getMirostatEta())
				.penalizeNewline(fromOptions.getPenalizeNewline())
				.stop(fromOptions.getStop())
				.toolNames(fromOptions.getToolNames())
				.internalToolExecutionEnabled(fromOptions.getInternalToolExecutionEnabled())
				.toolCallbacks(fromOptions.getToolCallbacks())
				.toolContext(fromOptions.getToolContext()).build();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Object getFormat() {
		return this.format;
	}

	public void setFormat(Object format) {
		this.format = format;
	}

	public String getKeepAlive() {
		return this.keepAlive;
	}

	public void setKeepAlive(String keepAlive) {
		this.keepAlive = keepAlive;
	}

	public Boolean getUseNUMA() {
		return this.useNUMA;
	}

	public void setUseNUMA(Boolean useNUMA) {
		this.useNUMA = useNUMA;
	}

	public Integer getNumCtx() {
		return this.numCtx;
	}

	public void setNumCtx(Integer numCtx) {
		this.numCtx = numCtx;
	}

	public Integer getNumBatch() {
		return this.numBatch;
	}

	public void setNumBatch(Integer numBatch) {
		this.numBatch = numBatch;
	}

	public Integer getNumGPU() {
		return this.numGPU;
	}

	public void setNumGPU(Integer numGPU) {
		this.numGPU = numGPU;
	}

	public Integer getMainGPU() {
		return this.mainGPU;
	}

	public void setMainGPU(Integer mainGPU) {
		this.mainGPU = mainGPU;
	}

	public Boolean getLowVRAM() {
		return this.lowVRAM;
	}

	public void setLowVRAM(Boolean lowVRAM) {
		this.lowVRAM = lowVRAM;
	}

	public Boolean getF16KV() {
		return this.f16KV;
	}

	public void setF16KV(Boolean f16kv) {
		this.f16KV = f16kv;
	}

	public Boolean getLogitsAll() {
		return this.logitsAll;
	}

	public void setLogitsAll(Boolean logitsAll) {
		this.logitsAll = logitsAll;
	}

	public Boolean getVocabOnly() {
		return this.vocabOnly;
	}

	public void setVocabOnly(Boolean vocabOnly) {
		this.vocabOnly = vocabOnly;
	}

	public Boolean getUseMMap() {
		return this.useMMap;
	}

	public void setUseMMap(Boolean useMMap) {
		this.useMMap = useMMap;
	}

	public Boolean getUseMLock() {
		return this.useMLock;
	}

	public void setUseMLock(Boolean useMLock) {
		this.useMLock = useMLock;
	}

	public Integer getNumThread() {
		return this.numThread;
	}

	public void setNumThread(Integer numThread) {
		this.numThread = numThread;
	}

	public Integer getNumKeep() {
		return this.numKeep;
	}

	public void setNumKeep(Integer numKeep) {
		this.numKeep = numKeep;
	}

	public Integer getSeed() {
		return this.seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	@Override
	@JsonIgnore
	public Integer getMaxTokens() {
		return getNumPredict();
	}

	@JsonIgnore
	public void setMaxTokens(Integer maxTokens) {
		setNumPredict(maxTokens);
	}

	public Integer getNumPredict() {
		return this.numPredict;
	}

	public void setNumPredict(Integer numPredict) {
		this.numPredict = numPredict;
	}

	@Override
	public Integer getTopK() {
		return this.topK;
	}

	public void setTopK(Integer topK) {
		this.topK = topK;
	}

	@Override
	public Double getTopP() {
		return this.topP;
	}

	public void setTopP(Double topP) {
		this.topP = topP;
	}

	public Double getMinP() {
		return this.minP;
	}

	public void setMinP(Double minP) {
		this.minP = minP;
	}

	public Float getTfsZ() {
		return this.tfsZ;
	}

	public void setTfsZ(Float tfsZ) {
		this.tfsZ = tfsZ;
	}

	public Float getTypicalP() {
		return this.typicalP;
	}

	public void setTypicalP(Float typicalP) {
		this.typicalP = typicalP;
	}

	public Integer getRepeatLastN() {
		return this.repeatLastN;
	}

	public void setRepeatLastN(Integer repeatLastN) {
		this.repeatLastN = repeatLastN;
	}

	@Override
	public Double getTemperature() {
		return this.temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public Double getRepeatPenalty() {
		return this.repeatPenalty;
	}

	public void setRepeatPenalty(Double repeatPenalty) {
		this.repeatPenalty = repeatPenalty;
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

	public Integer getMirostat() {
		return this.mirostat;
	}

	public void setMirostat(Integer mirostat) {
		this.mirostat = mirostat;
	}

	public Float getMirostatTau() {
		return this.mirostatTau;
	}

	public void setMirostatTau(Float mirostatTau) {
		this.mirostatTau = mirostatTau;
	}

	public Float getMirostatEta() {
		return this.mirostatEta;
	}

	public void setMirostatEta(Float mirostatEta) {
		this.mirostatEta = mirostatEta;
	}

	public Boolean getPenalizeNewline() {
		return this.penalizeNewline;
	}

	public void setPenalizeNewline(Boolean penalizeNewline) {
		this.penalizeNewline = penalizeNewline;
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

	public Boolean getTruncate() {
		return this.truncate;
	}

	public void setTruncate(Boolean truncate) {
		this.truncate = truncate;
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
	public void setFunctions(Set<String> functions) {
		this.setToolNames(functions);
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	@Override
	@Deprecated
	@JsonIgnore
	public Boolean getProxyToolCalls() {
		return this.internalToolExecutionEnabled != null ? !this.internalToolExecutionEnabled : null;
	}

	@Deprecated
	@JsonIgnore
	public void setProxyToolCalls(Boolean proxyToolCalls) {
		this.internalToolExecutionEnabled = proxyToolCalls != null ? !proxyToolCalls : null;
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

	public Map<String, Object> toMap() {
		return ModelOptionsUtils.objectToMap(this);
	}

	@Override
	public OllamaOptions copy() {
		return fromOptions(this);
	}
	// @formatter:on

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		OllamaOptions that = (OllamaOptions) o;
		return Objects.equals(this.model, that.model) && Objects.equals(this.format, that.format)
				&& Objects.equals(this.keepAlive, that.keepAlive) && Objects.equals(this.truncate, that.truncate)
				&& Objects.equals(this.useNUMA, that.useNUMA) && Objects.equals(this.numCtx, that.numCtx)
				&& Objects.equals(this.numBatch, that.numBatch) && Objects.equals(this.numGPU, that.numGPU)
				&& Objects.equals(this.mainGPU, that.mainGPU) && Objects.equals(this.lowVRAM, that.lowVRAM)
				&& Objects.equals(this.f16KV, that.f16KV) && Objects.equals(this.logitsAll, that.logitsAll)
				&& Objects.equals(this.vocabOnly, that.vocabOnly) && Objects.equals(this.useMMap, that.useMMap)
				&& Objects.equals(this.useMLock, that.useMLock) && Objects.equals(this.numThread, that.numThread)
				&& Objects.equals(this.numKeep, that.numKeep) && Objects.equals(this.seed, that.seed)
				&& Objects.equals(this.numPredict, that.numPredict) && Objects.equals(this.topK, that.topK)
				&& Objects.equals(this.topP, that.topP) && Objects.equals(this.minP, that.minP)
				&& Objects.equals(this.tfsZ, that.tfsZ) && Objects.equals(this.typicalP, that.typicalP)
				&& Objects.equals(this.repeatLastN, that.repeatLastN)
				&& Objects.equals(this.temperature, that.temperature)
				&& Objects.equals(this.repeatPenalty, that.repeatPenalty)
				&& Objects.equals(this.presencePenalty, that.presencePenalty)
				&& Objects.equals(this.frequencyPenalty, that.frequencyPenalty)
				&& Objects.equals(this.mirostat, that.mirostat) && Objects.equals(this.mirostatTau, that.mirostatTau)
				&& Objects.equals(this.mirostatEta, that.mirostatEta)
				&& Objects.equals(this.penalizeNewline, that.penalizeNewline) && Objects.equals(this.stop, that.stop)
				&& Objects.equals(this.toolCallbacks, that.toolCallbacks)
				&& Objects.equals(this.internalToolExecutionEnabled, that.internalToolExecutionEnabled)
				&& Objects.equals(this.toolNames, that.toolNames) && Objects.equals(this.toolContext, that.toolContext);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.model, this.format, this.keepAlive, this.truncate, this.useNUMA, this.numCtx,
				this.numBatch, this.numGPU, this.mainGPU, this.lowVRAM, this.f16KV, this.logitsAll, this.vocabOnly,
				this.useMMap, this.useMLock, this.numThread, this.numKeep, this.seed, this.numPredict, this.topK,
				this.topP, this.minP, this.tfsZ, this.typicalP, this.repeatLastN, this.temperature, this.repeatPenalty,
				this.presencePenalty, this.frequencyPenalty, this.mirostat, this.mirostatTau, this.mirostatEta,
				this.penalizeNewline, this.stop, this.toolCallbacks, this.toolNames, this.internalToolExecutionEnabled,
				this.toolContext);
	}

	public static class Builder {

		private final OllamaOptions options = new OllamaOptions();

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder model(OllamaModel model) {
			this.options.model = model.getName();
			return this;
		}

		public Builder format(Object format) {
			this.options.format = format;
			return this;
		}

		public Builder keepAlive(String keepAlive) {
			this.options.keepAlive = keepAlive;
			return this;
		}

		public Builder truncate(Boolean truncate) {
			this.options.truncate = truncate;
			return this;
		}

		public Builder useNUMA(Boolean useNUMA) {
			this.options.useNUMA = useNUMA;
			return this;
		}

		public Builder numCtx(Integer numCtx) {
			this.options.numCtx = numCtx;
			return this;
		}

		public Builder numBatch(Integer numBatch) {
			this.options.numBatch = numBatch;
			return this;
		}

		public Builder numGPU(Integer numGPU) {
			this.options.numGPU = numGPU;
			return this;
		}

		public Builder mainGPU(Integer mainGPU) {
			this.options.mainGPU = mainGPU;
			return this;
		}

		public Builder lowVRAM(Boolean lowVRAM) {
			this.options.lowVRAM = lowVRAM;
			return this;
		}

		public Builder f16KV(Boolean f16KV) {
			this.options.f16KV = f16KV;
			return this;
		}

		public Builder logitsAll(Boolean logitsAll) {
			this.options.logitsAll = logitsAll;
			return this;
		}

		public Builder vocabOnly(Boolean vocabOnly) {
			this.options.vocabOnly = vocabOnly;
			return this;
		}

		public Builder useMMap(Boolean useMMap) {
			this.options.useMMap = useMMap;
			return this;
		}

		public Builder useMLock(Boolean useMLock) {
			this.options.useMLock = useMLock;
			return this;
		}

		public Builder numThread(Integer numThread) {
			this.options.numThread = numThread;
			return this;
		}

		public Builder numKeep(Integer numKeep) {
			this.options.numKeep = numKeep;
			return this;
		}

		public Builder seed(Integer seed) {
			this.options.seed = seed;
			return this;
		}

		public Builder numPredict(Integer numPredict) {
			this.options.numPredict = numPredict;
			return this;
		}

		public Builder topK(Integer topK) {
			this.options.topK = topK;
			return this;
		}

		public Builder topP(Double topP) {
			this.options.topP = topP;
			return this;
		}

		public Builder minP(Double minP) {
			this.options.minP = minP;
			return this;
		}

		public Builder tfsZ(Float tfsZ) {
			this.options.tfsZ = tfsZ;
			return this;
		}

		public Builder typicalP(Float typicalP) {
			this.options.typicalP = typicalP;
			return this;
		}

		public Builder repeatLastN(Integer repeatLastN) {
			this.options.repeatLastN = repeatLastN;
			return this;
		}

		public Builder temperature(Double temperature) {
			this.options.temperature = temperature;
			return this;
		}

		public Builder repeatPenalty(Double repeatPenalty) {
			this.options.repeatPenalty = repeatPenalty;
			return this;
		}

		public Builder presencePenalty(Double presencePenalty) {
			this.options.presencePenalty = presencePenalty;
			return this;
		}

		public Builder frequencyPenalty(Double frequencyPenalty) {
			this.options.frequencyPenalty = frequencyPenalty;
			return this;
		}

		public Builder mirostat(Integer mirostat) {
			this.options.mirostat = mirostat;
			return this;
		}

		public Builder mirostatTau(Float mirostatTau) {
			this.options.mirostatTau = mirostatTau;
			return this;
		}

		public Builder mirostatEta(Float mirostatEta) {
			this.options.mirostatEta = mirostatEta;
			return this;
		}

		public Builder penalizeNewline(Boolean penalizeNewline) {
			this.options.penalizeNewline = penalizeNewline;
			return this;
		}

		public Builder stop(List<String> stop) {
			this.options.stop = stop;
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
		public Builder functions(Set<String> functions) {
			return toolNames(functions);
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

		public Builder toolContext(Map<String, Object> toolContext) {
			if (this.options.toolContext == null) {
				this.options.toolContext = toolContext;
			}
			else {
				this.options.toolContext.putAll(toolContext);
			}
			return this;
		}

		public OllamaOptions build() {
			return this.options;
		}

	}

}
