package org.springframework.ai.oci.cohere;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oracle.bmc.generativeaiinference.model.CohereTool;

import org.springframework.ai.chat.prompt.ChatOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OCICohereChatOptions implements ChatOptions {

	@JsonProperty("model")
	private String model;

	@JsonProperty("maxTokens")
	private Integer maxTokens;

	@JsonProperty("compartment")
	private String compartment;

	@JsonProperty("servingMode")
	private String servingMode;

	@JsonProperty("preambleOverride")
	private String preambleOverride;

	@JsonProperty("temperature")
	private Double temperature;

	@JsonProperty("topP")
	private Double topP;

	@JsonProperty("topK")
	private Integer topK;

	@JsonProperty("frequencyPenalty")
	private Double frequencyPenalty;

	@JsonProperty("presencePenalty")
	private Double presencePenalty;

	@JsonProperty("stop")
	private List<String> stop;

	@JsonProperty("documents")
	private List<Object> documents;

	@JsonProperty("tools")
	private List<CohereTool> tools;

	public static OCICohereChatOptions fromOptions(OCICohereChatOptions fromOptions) {
		return builder().model(fromOptions.model)
			.maxTokens(fromOptions.maxTokens)
			.compartment(fromOptions.compartment)
			.servingMode(fromOptions.servingMode)
			.preambleOverride(fromOptions.preambleOverride)
			.temperature(fromOptions.temperature)
			.topP(fromOptions.topP)
			.topK(fromOptions.topK)
			.stop(fromOptions.stop != null ? new ArrayList<>(fromOptions.stop) : null)
			.frequencyPenalty(fromOptions.frequencyPenalty)
			.presencePenalty(fromOptions.presencePenalty)
			.documents(fromOptions.documents != null ? new ArrayList<>(fromOptions.documents) : null)
			.tools(fromOptions.tools != null ? new ArrayList<>(fromOptions.tools) : null)
			.build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public void setPresencePenalty(Double presencePenalty) {
		this.presencePenalty = presencePenalty;
	}

	public void setFrequencyPenalty(Double frequencyPenalty) {
		this.frequencyPenalty = frequencyPenalty;
	}

	public void setTopK(Integer topK) {
		this.topK = topK;
	}

	public void setTopP(Double topP) {
		this.topP = topP;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public String getPreambleOverride() {
		return this.preambleOverride;
	}

	public void setPreambleOverride(String preambleOverride) {
		this.preambleOverride = preambleOverride;
	}

	public String getServingMode() {
		return this.servingMode;
	}

	public void setServingMode(String servingMode) {
		this.servingMode = servingMode;
	}

	public String getCompartment() {
		return this.compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<String> getStop() {
		return this.stop;
	}

	public void setStop(List<String> stop) {
		this.stop = stop;
	}

	public List<Object> getDocuments() {
		return this.documents;
	}

	public void setDocuments(List<Object> documents) {
		this.documents = documents;
	}

	public List<CohereTool> getTools() {
		return this.tools;
	}

	public void setTools(List<CohereTool> tools) {
		this.tools = tools;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	@Override
	public Double getFrequencyPenalty() {
		return this.frequencyPenalty;
	}

	@Override
	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	@Override
	public Double getPresencePenalty() {
		return this.presencePenalty;
	}

	@Override
	public List<String> getStopSequences() {
		return this.stop;
	}

	@Override
	public Double getTemperature() {
		return this.temperature;
	}

	@Override
	public Integer getTopK() {
		return this.topK;
	}

	@Override
	public Double getTopP() {
		return this.topP;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ChatOptions copy() {
		return fromOptions(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(model, maxTokens, compartment, servingMode, preambleOverride, temperature, topP, topK, stop,
				frequencyPenalty, presencePenalty, documents, tools);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		OCICohereChatOptions that = (OCICohereChatOptions) o;

		return Objects.equals(this.model, that.model) && Objects.equals(this.maxTokens, that.maxTokens)
				&& Objects.equals(this.compartment, that.compartment)
				&& Objects.equals(this.servingMode, that.servingMode)
				&& Objects.equals(this.preambleOverride, that.preambleOverride)
				&& Objects.equals(this.temperature, that.temperature) && Objects.equals(this.topP, that.topP)
				&& Objects.equals(this.topK, that.topK) && Objects.equals(this.stop, that.stop)
				&& Objects.equals(this.frequencyPenalty, that.frequencyPenalty)
				&& Objects.equals(this.presencePenalty, that.presencePenalty)
				&& Objects.equals(this.documents, that.documents) && Objects.equals(this.tools, that.tools);
	}

	public static class Builder {

		protected OCICohereChatOptions chatOptions;

		public Builder() {
			this.chatOptions = new OCICohereChatOptions();
		}

		public Builder(OCICohereChatOptions chatOptions) {
			this.chatOptions = chatOptions;
		}

		public Builder model(String model) {
			this.chatOptions.model = model;
			return this;
		}

		public Builder maxTokens(Integer maxTokens) {
			this.chatOptions.maxTokens = maxTokens;
			return this;
		}

		public Builder compartment(String compartment) {
			this.chatOptions.compartment = compartment;
			return this;
		}

		public Builder servingMode(String servingMode) {
			this.chatOptions.servingMode = servingMode;
			return this;
		}

		public Builder preambleOverride(String preambleOverride) {
			this.chatOptions.preambleOverride = preambleOverride;
			return this;
		}

		public Builder temperature(Double temperature) {
			this.chatOptions.temperature = temperature;
			return this;
		}

		public Builder topP(Double topP) {
			this.chatOptions.topP = topP;
			return this;
		}

		public Builder topK(Integer topK) {
			this.chatOptions.topK = topK;
			return this;
		}

		public Builder frequencyPenalty(Double frequencyPenalty) {
			this.chatOptions.frequencyPenalty = frequencyPenalty;
			return this;
		}

		public Builder presencePenalty(Double presencePenalty) {
			this.chatOptions.presencePenalty = presencePenalty;
			return this;
		}

		public Builder stop(List<String> stop) {
			this.chatOptions.stop = stop;
			return this;
		}

		public Builder documents(List<Object> documents) {
			this.chatOptions.documents = documents;
			return this;
		}

		public Builder tools(List<CohereTool> tools) {
			this.chatOptions.tools = tools;
			return this;
		}

		public OCICohereChatOptions build() {
			return this.chatOptions;
		}

	}

}
