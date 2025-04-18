package org.springframework.ai.minimax;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(Include.NON_NULL)
public class MiniMaxEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("model") String model;
	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	public static class Builder {

		protected MiniMaxEmbeddingOptions options;

		public Builder() {
			this.options = new MiniMaxEmbeddingOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public MiniMaxEmbeddingOptions build() {
			return this.options;
		}

	}

}
