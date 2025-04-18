package org.springframework.ai.watsonx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.prompt.ChatOptions;

// @formatter:off

public class WatsonxAiChatOptions implements ChatOptions {

	@JsonIgnore
	private final ObjectMapper mapper = new ObjectMapper();

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("top_p")
	private Double topP;

	@JsonProperty("top_k")
	private Integer topK;

	@JsonProperty("decoding_method")
	private String decodingMethod;

	@JsonProperty("max_new_tokens")
	private Integer maxNewTokens;

	@JsonProperty("min_new_tokens")
	private Integer minNewTokens;

	@JsonProperty("stop_sequences")
	private List<String> stopSequences;

	@JsonProperty("repetition_penalty")
	private Double repetitionPenalty;

	@JsonProperty("random_seed")
	private Integer randomSeed;

	@JsonProperty("model")
	private String model;

	@JsonProperty("additional")
	private Map<String, Object> additional = new HashMap<>();

	public static Builder builder() {
		return new Builder();
	}

	public static Map<String, Object> filterNonSupportedFields(Map<String, Object> options) {
		return options.entrySet().stream()
				.filter(e -> !e.getKey().equals("model"))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public static WatsonxAiChatOptions fromOptions(WatsonxAiChatOptions fromOptions) {
		return WatsonxAiChatOptions.builder()
				.temperature(fromOptions.getTemperature())
				.topP(fromOptions.getTopP())
				.topK(fromOptions.getTopK())
				.decodingMethod(fromOptions.getDecodingMethod())
				.maxNewTokens(fromOptions.getMaxNewTokens())
				.minNewTokens(fromOptions.getMinNewTokens())
				.stopSequences(fromOptions.getStopSequences())
				.repetitionPenalty(fromOptions.getRepetitionPenalty())
				.randomSeed(fromOptions.getRandomSeed())
				.model(fromOptions.getModel())
				.additionalProperties(fromOptions.getAdditionalProperties())
				.build();
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

	@Override
	public Integer getTopK() {
		return this.topK;
	}

	public void setTopK(Integer topK) {
		this.topK = topK;
	}

	public String getDecodingMethod() {
		return this.decodingMethod;
	}

	public void setDecodingMethod(String decodingMethod) {
		this.decodingMethod = decodingMethod;
	}

	@Override
	@JsonIgnore
	public Integer getMaxTokens() {
		return getMaxNewTokens();
	}

	@JsonIgnore
	public void setMaxTokens(Integer maxTokens) {
		setMaxNewTokens(maxTokens);
	}

	public Integer getMaxNewTokens() {
		return this.maxNewTokens;
	}

	public void setMaxNewTokens(Integer maxNewTokens) {
		this.maxNewTokens = maxNewTokens;
	}

	public Integer getMinNewTokens() {
		return this.minNewTokens;
	}

	public void setMinNewTokens(Integer minNewTokens) {
		this.minNewTokens = minNewTokens;
	}

	@Override
	public List<String> getStopSequences() {
		return this.stopSequences;
	}

	public void setStopSequences(List<String> stopSequences) {
		this.stopSequences = stopSequences;
	}

	@Override
	@JsonIgnore
	public Double getPresencePenalty() {
		return getRepetitionPenalty();
	}

	@JsonIgnore
	public void setPresencePenalty(Double presencePenalty) {
		setRepetitionPenalty(presencePenalty);
	}

	public Double getRepetitionPenalty() {
		return this.repetitionPenalty;
	}

	public void setRepetitionPenalty(Double repetitionPenalty) {
		this.repetitionPenalty = repetitionPenalty;
	}

	public Integer getRandomSeed() {
		return this.randomSeed;
	}

	public void setRandomSeed(Integer randomSeed) {
		this.randomSeed = randomSeed;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additional.entrySet().stream()
				.collect(Collectors.toMap(
						entry -> toSnakeCase(entry.getKey()),
						Map.Entry::getValue
				));
	}

	@JsonAnySetter
	public void addAdditionalProperty(String key, Object value) {
		this.additional.put(key, value);
	}

	@Override
	@JsonIgnore
	public Double getFrequencyPenalty() {
		return null;
	}

	public Map<String, Object> toMap() {
		try {
			var json = this.mapper.writeValueAsString(this);
			var map = this.mapper.readValue(json, new TypeReference<Map<String, Object>>() { });
			map.remove("additional");

			return map;
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String toSnakeCase(String input) {
		return input != null ? input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase() : null;
	}

	@Override
	public WatsonxAiChatOptions copy() {
		return fromOptions(this);
	}

	public static class Builder {

		WatsonxAiChatOptions options = new WatsonxAiChatOptions();

		public Builder temperature(Double temperature) {
			this.options.temperature = temperature;
			return this;
		}

		public Builder topP(Double topP) {
			this.options.topP = topP;
			return this;
		}

		public Builder topK(Integer topK) {
			this.options.topK = topK;
			return this;
		}

		public Builder decodingMethod(String decodingMethod) {
			this.options.decodingMethod = decodingMethod;
			return this;
		}

		public Builder maxNewTokens(Integer maxNewTokens) {
			this.options.maxNewTokens = maxNewTokens;
			return this;
		}

		public Builder minNewTokens(Integer minNewTokens) {
			this.options.minNewTokens = minNewTokens;
			return this;
		}

		public Builder stopSequences(List<String> stopSequences) {
			this.options.stopSequences = stopSequences;
			return this;
		}

		public Builder repetitionPenalty(Double repetitionPenalty) {
			this.options.repetitionPenalty = repetitionPenalty;
			return this;
		}

		public Builder randomSeed(Integer randomSeed) {
			this.options.randomSeed = randomSeed;
			return this;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder additionalProperty(String key, Object value) {
			this.options.additional.put(key, value);
			return this;
		}

		public Builder additionalProperties(Map<String, Object> properties) {
			this.options.additional.putAll(properties);
			return this;
		}

		public WatsonxAiChatOptions build() {
			return this.options;
		}
	}

}
// @formatter:on
