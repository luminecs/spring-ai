package org.springframework.ai.zhipuai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(Include.NON_NULL)
public class ZhiPuAiEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("dimensions") Integer dimensions;
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

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	public static class Builder {

		protected ZhiPuAiEmbeddingOptions options;

		public Builder() {
			this.options = new ZhiPuAiEmbeddingOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder dimensions(Integer dimensions) {
			this.options.setDimensions(dimensions);
			return this;
		}

		public ZhiPuAiEmbeddingOptions build() {
			return this.options;
		}

	}

}
