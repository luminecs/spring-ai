package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(Include.NON_NULL)
public class OpenAiEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("encoding_format") String encodingFormat;

	private @JsonProperty("dimensions") Integer dimensions;

	private @JsonProperty("user") String user;
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

	public String getEncodingFormat() {
		return this.encodingFormat;
	}

	public void setEncodingFormat(String encodingFormat) {
		this.encodingFormat = encodingFormat;
	}

	@Override
	public Integer getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public static class Builder {

		protected OpenAiEmbeddingOptions options;

		public Builder() {
			this.options = new OpenAiEmbeddingOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder encodingFormat(String encodingFormat) {
			this.options.setEncodingFormat(encodingFormat);
			return this;
		}

		public Builder dimensions(Integer dimensions) {
			this.options.dimensions = dimensions;
			return this;
		}

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public OpenAiEmbeddingOptions build() {
			return this.options;
		}

	}

}
