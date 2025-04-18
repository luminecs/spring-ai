package org.springframework.ai.azure.openai;

import java.util.List;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionTimestampGranularity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.util.Assert;

@JsonInclude(Include.NON_NULL)
public class AzureOpenAiAudioTranscriptionOptions implements AudioTranscriptionOptions {

	public static final String DEFAULT_AUDIO_TRANSCRIPTION_MODEL = WhisperModel.WHISPER.getValue();

	// @formatter:off

	private @JsonProperty("model") String model = DEFAULT_AUDIO_TRANSCRIPTION_MODEL;

	private @JsonProperty("deployment_name") String deploymentName;

	private @JsonProperty("response_format") TranscriptResponseFormat responseFormat = TranscriptResponseFormat.JSON;

	private @JsonProperty("prompt") String prompt;

	private @JsonProperty("language") String language;

	private @JsonProperty("temperature") Float temperature = 0F;

	private @JsonProperty("timestamp_granularities") List<GranularityType> granularityType;

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

	public String getDeploymentName() {
		return this.deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
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

	public List<GranularityType> getGranularityType() {
		return this.granularityType;
	}

	public void setGranularityType(List<GranularityType> granularityType) {
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
		AzureOpenAiAudioTranscriptionOptions other = (AzureOpenAiAudioTranscriptionOptions) obj;
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
			return other.responseFormat == null;
		}
		else {
			return this.responseFormat.equals(other.responseFormat);
		}
	}

	public enum WhisperModel {

		// @formatter:off
		@JsonProperty("whisper")
		WHISPER("whisper");
		// @formatter:on

		public final String value;

		WhisperModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}

	public enum TranscriptResponseFormat {

		// @formatter:off
		@JsonProperty("json")
		JSON(AudioTranscriptionFormat.JSON, StructuredResponse.class),
		@JsonProperty("text")
		TEXT(AudioTranscriptionFormat.TEXT, String.class),
		@JsonProperty("srt")
		SRT(AudioTranscriptionFormat.SRT, String.class),
		@JsonProperty("verbose_json")
		VERBOSE_JSON(AudioTranscriptionFormat.VERBOSE_JSON, StructuredResponse.class),
		@JsonProperty("vtt")
		VTT(AudioTranscriptionFormat.VTT, String.class);

		public final AudioTranscriptionFormat value;

		public final Class<?> responseType;

		TranscriptResponseFormat(AudioTranscriptionFormat value, Class<?> responseType) {
			this.value = value;
			this.responseType = responseType;
		}

		public AudioTranscriptionFormat getValue() {
			return this.value;
		}

		public Class<?> getResponseType() {
			return this.responseType;
		}
	}

	public enum GranularityType {

		// @formatter:off
		@JsonProperty("word")
		WORD(AudioTranscriptionTimestampGranularity.WORD),
		@JsonProperty("segment")
		SEGMENT(AudioTranscriptionTimestampGranularity.SEGMENT);
		// @formatter:on

		public final AudioTranscriptionTimestampGranularity value;

		GranularityType(AudioTranscriptionTimestampGranularity value) {
			this.value = value;
		}

		public AudioTranscriptionTimestampGranularity getValue() {
			return this.value;
		}

	}

	public static class Builder {

		protected AzureOpenAiAudioTranscriptionOptions options;

		public Builder() {
			this.options = new AzureOpenAiAudioTranscriptionOptions();
		}

		public Builder(AzureOpenAiAudioTranscriptionOptions options) {
			this.options = options;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder deploymentName(String deploymentName) {
			this.options.setDeploymentName(deploymentName);
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

		public Builder granularityType(List<GranularityType> granularityType) {
			this.options.granularityType = granularityType;
			return this;
		}

		public AzureOpenAiAudioTranscriptionOptions build() {
			Assert.hasText(this.options.model, "model must not be empty");
			Assert.notNull(this.options.responseFormat, "response_format must not be null");

			return this.options;
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record StructuredResponse(
	// @formatter:off
		@JsonProperty("language") String language,
		@JsonProperty("duration") Float duration,
		@JsonProperty("text") String text,
		@JsonProperty("words") List<Word> words,
		@JsonProperty("segments") List<Segment> segments) {
		// @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record Word(
		// @formatter:off
			@JsonProperty("word") String word,
			@JsonProperty("start") Float start,
			@JsonProperty("end") Float end) {
			// @formatter:on
		}

		@JsonInclude(Include.NON_NULL)
		public record Segment(
		// @formatter:off
				@JsonProperty("id") Integer id,
				@JsonProperty("seek") Integer seek,
				@JsonProperty("start") Float start,
				@JsonProperty("end") Float end,
				@JsonProperty("text") String text,
				@JsonProperty("tokens") List<Integer> tokens,
				@JsonProperty("temperature") Float temperature,
				@JsonProperty("avg_logprob") Float avgLogprob,
				@JsonProperty("compression_ratio") Float compressionRatio,
				@JsonProperty("no_speech_prob") Float noSpeechProb) {
			// @formatter:on
		}

	}

}
