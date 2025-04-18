package org.springframework.ai.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.Voice;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiAudioSpeechOptions implements ModelOptions {

	@JsonProperty("model")
	private String model;

	@JsonProperty("input")
	private String input;

	@JsonProperty("voice")
	private String voice;

	@JsonProperty("response_format")
	private AudioResponseFormat responseFormat;

	@JsonProperty("speed")
	private Float speed;

	public static Builder builder() {
		return new Builder();
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getInput() {
		return this.input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getVoice() {
		return this.voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public void setVoice(Voice voice) {
		this.voice = voice.getValue();
	}

	public AudioResponseFormat getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(AudioResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public Float getSpeed() {
		return this.speed;
	}

	public void setSpeed(Float speed) {
		this.speed = speed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.model == null) ? 0 : this.model.hashCode());
		result = prime * result + ((this.input == null) ? 0 : this.input.hashCode());
		result = prime * result + ((this.voice == null) ? 0 : this.voice.hashCode());
		result = prime * result + ((this.responseFormat == null) ? 0 : this.responseFormat.hashCode());
		result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
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
		OpenAiAudioSpeechOptions other = (OpenAiAudioSpeechOptions) obj;
		if (this.model == null) {
			if (other.model != null) {
				return false;
			}
		}
		else if (!this.model.equals(other.model)) {
			return false;
		}
		if (this.input == null) {
			if (other.input != null) {
				return false;
			}
		}
		else if (!this.input.equals(other.input)) {
			return false;
		}
		if (this.voice == null) {
			if (other.voice != null) {
				return false;
			}
		}
		else if (!this.voice.equals(other.voice)) {
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
		if (this.speed == null) {
			return other.speed == null;
		}
		else {
			return this.speed.equals(other.speed);
		}
	}

	@Override
	public String toString() {
		return "OpenAiAudioSpeechOptions{" + "model='" + this.model + '\'' + ", input='" + this.input + '\''
				+ ", voice='" + this.voice + '\'' + ", responseFormat='" + this.responseFormat + '\'' + ", speed="
				+ this.speed + '}';
	}

	public static class Builder {

		private final OpenAiAudioSpeechOptions options = new OpenAiAudioSpeechOptions();

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder input(String input) {
			this.options.input = input;
			return this;
		}

		public Builder voice(String voice) {
			this.options.voice = voice;
			return this;
		}

		public Builder voice(Voice voice) {
			this.options.voice = voice.getValue();
			return this;
		}

		public Builder responseFormat(AudioResponseFormat responseFormat) {
			this.options.responseFormat = responseFormat;
			return this;
		}

		public Builder speed(Float speed) {
			this.options.speed = speed;
			return this;
		}

		public OpenAiAudioSpeechOptions build() {
			return this.options;
		}

	}

}
