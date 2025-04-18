package org.springframework.ai.bedrock.cohere;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest.InputType;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest.Truncate;
import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(Include.NON_NULL)
public class BedrockCohereEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("input_type") InputType inputType;

	private @JsonProperty("truncate") Truncate truncate;
	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	public InputType getInputType() {
		return this.inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	public Truncate getTruncate() {
		return this.truncate;
	}

	public void setTruncate(Truncate truncate) {
		this.truncate = truncate;
	}

	@Override
	@JsonIgnore
	public String getModel() {
		return null;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	public static class Builder {

		private BedrockCohereEmbeddingOptions options = new BedrockCohereEmbeddingOptions();

		public Builder inputType(InputType inputType) {
			this.options.setInputType(inputType);
			return this;
		}

		public Builder truncate(Truncate truncate) {
			this.options.setTruncate(truncate);
			return this;
		}

		public BedrockCohereEmbeddingOptions build() {
			return this.options;
		}

	}

}
