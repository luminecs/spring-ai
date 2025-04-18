package org.springframework.ai.qianfan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(Include.NON_NULL)
public class QianFanEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("user_id") String user;
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

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	public static class Builder {

		protected QianFanEmbeddingOptions options;

		public Builder() {
			this.options = new QianFanEmbeddingOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public QianFanEmbeddingOptions build() {
			return this.options;
		}

	}

}
