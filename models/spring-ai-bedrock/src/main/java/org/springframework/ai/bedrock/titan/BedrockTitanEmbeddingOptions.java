package org.springframework.ai.bedrock.titan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel.InputType;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class BedrockTitanEmbeddingOptions implements EmbeddingOptions {

	private InputType inputType;

	public static Builder builder() {
		return new Builder();
	}

	public InputType getInputType() {
		return this.inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
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

		private BedrockTitanEmbeddingOptions options = new BedrockTitanEmbeddingOptions();

		public Builder withInputType(InputType inputType) {
			Assert.notNull(inputType, "input type can not be null.");

			this.options.setInputType(inputType);
			return this;
		}

		public BedrockTitanEmbeddingOptions build() {
			return this.options;
		}

	}

}
