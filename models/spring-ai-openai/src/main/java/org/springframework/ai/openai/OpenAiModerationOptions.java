package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.moderation.ModerationOptions;
import org.springframework.ai.openai.api.OpenAiModerationApi;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiModerationOptions implements ModerationOptions {

	@JsonProperty("model")
	private String model = OpenAiModerationApi.DEFAULT_MODERATION_MODEL;

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

	public static final class Builder {

		private final OpenAiModerationOptions options;

		private Builder() {
			this.options = new OpenAiModerationOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public OpenAiModerationOptions build() {
			return this.options;
		}

	}

}
