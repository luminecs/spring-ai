package org.springframework.ai.openai.metadata.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.metadata.OpenAiRateLimit;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class OpenAiAudioTranscriptionResponseMetadata extends AudioTranscriptionResponseMetadata {

	public static final OpenAiAudioTranscriptionResponseMetadata NULL = new OpenAiAudioTranscriptionResponseMetadata() {

	};

	protected static final String AI_METADATA_STRING = "{ @type: %1$s, rateLimit: %4$s }";

	@Nullable
	private RateLimit rateLimit;

	protected OpenAiAudioTranscriptionResponseMetadata() {
		this(null);
	}

	protected OpenAiAudioTranscriptionResponseMetadata(@Nullable OpenAiRateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}

	public static OpenAiAudioTranscriptionResponseMetadata from(OpenAiAudioApi.StructuredResponse result) {
		Assert.notNull(result, "OpenAI Transcription must not be null");
		return new OpenAiAudioTranscriptionResponseMetadata();
	}

	public static OpenAiAudioTranscriptionResponseMetadata from(String result) {
		Assert.notNull(result, "OpenAI Transcription must not be null");
		return new OpenAiAudioTranscriptionResponseMetadata();
	}

	@Nullable
	public RateLimit getRateLimit() {
		RateLimit rateLimit = this.rateLimit;
		return rateLimit != null ? rateLimit : new EmptyRateLimit();
	}

	public OpenAiAudioTranscriptionResponseMetadata withRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
		return this;
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getClass().getName(), getRateLimit());
	}

}
