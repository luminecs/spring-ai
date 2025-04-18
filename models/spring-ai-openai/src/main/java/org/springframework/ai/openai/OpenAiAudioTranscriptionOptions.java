package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptionRequest.GranularityType;

@JsonInclude(Include.NON_NULL)
public class OpenAiAudioTranscriptionOptions implements AudioTranscriptionOptions {

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("response_format") TranscriptResponseFormat responseFormat;

	private @JsonProperty("prompt") String prompt;

	private @JsonProperty("language") String language;

	private @JsonProperty("temperature") Float temperature;

	private @JsonProperty("timestamp_granularities") GranularityType granularityType;

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

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPrompt() {
		return this.prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Float getTemperature() {
		return this.temperature;
	}

	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}

	public TranscriptResponseFormat getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(TranscriptResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public GranularityType getGranularityType() {
		return this.granularityType;
	}

	public void setGranularityType(GranularityType granularityType) {
		this.granularityType = granularityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.model == null) ? 0 : this.model.hashCode());
		result = prime * result + ((this.prompt == null) ? 0 : this.prompt.hashCode());
		result = prime * result + ((this.language == null) ? 0 : this.language.hashCode());
		result = prime * result + ((this.responseFormat == null) ? 0 : this.responseFormat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OpenAiAudioTranscriptionOptions other = (OpenAiAudioTranscriptionOptions) obj;
		if (this.model == null) {
			if (other.model != null) {
				return false;
			}
		}
		else if (!this.model.equals(other.model)) {
			return false;
		}
		if (this.prompt == null) {
			if (other.prompt != null) {
				return false;
			}
		}
		else if (!this.prompt.equals(other.prompt)) {
			return false;
		}
		if (this.language == null) {
			if (other.language != null) {
				return false;
			}
		}
		else if (!this.language.equals(other.language)) {
			return false;
		}
		if (this.responseFormat == null) {
			if (other.responseFormat != null) {
				return false;
			}
		}
		else if (!this.responseFormat.equals(other.responseFormat)) {
			return false;
		}
		return true;
	}

	public static class Builder {

		protected OpenAiAudioTranscriptionOptions options;

		public Builder() {
			this.options = new OpenAiAudioTranscriptionOptions();
		}

		public Builder(OpenAiAudioTranscriptionOptions options) {
			this.options = options;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder language(String language) {
			this.options.language = language;
			return this;
		}

		public Builder prompt(String prompt) {
			this.options.prompt = prompt;
			return this;
		}

		public Builder responseFormat(TranscriptResponseFormat responseFormat) {
			this.options.responseFormat = responseFormat;
			return this;
		}

		public Builder temperature(Float temperature) {
			this.options.temperature = temperature;
			return this;
		}

		public Builder granularityType(GranularityType granularityType) {
			this.options.granularityType = granularityType;
			return this;
		}

		public OpenAiAudioTranscriptionOptions build() {
			return this.options;
		}

	}
}
