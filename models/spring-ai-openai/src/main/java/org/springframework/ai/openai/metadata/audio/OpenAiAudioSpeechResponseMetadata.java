package org.springframework.ai.openai.metadata.audio;

import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.MutableResponseMetadata;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class OpenAiAudioSpeechResponseMetadata extends MutableResponseMetadata {

	public static final OpenAiAudioSpeechResponseMetadata NULL = new OpenAiAudioSpeechResponseMetadata() {

	};

	protected static final String AI_METADATA_STRING = "{ @type: %1$s, requestsLimit: %2$s }";

	@Nullable
	private RateLimit rateLimit;

	public OpenAiAudioSpeechResponseMetadata() {
		this(null);
	}

	public OpenAiAudioSpeechResponseMetadata(@Nullable RateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}

	public static OpenAiAudioSpeechResponseMetadata from(OpenAiAudioApi.StructuredResponse result) {
		Assert.notNull(result, "OpenAI speech must not be null");
		OpenAiAudioSpeechResponseMetadata speechResponseMetadata = new OpenAiAudioSpeechResponseMetadata();
		return speechResponseMetadata;
	}

	public static OpenAiAudioSpeechResponseMetadata from(String result) {
		Assert.notNull(result, "OpenAI speech must not be null");
		OpenAiAudioSpeechResponseMetadata speechResponseMetadata = new OpenAiAudioSpeechResponseMetadata();
		return speechResponseMetadata;
	}

	@Nullable
	public RateLimit getRateLimit() {
		RateLimit rateLimit = this.rateLimit;
		return rateLimit != null ? rateLimit : new EmptyRateLimit();
	}

	public OpenAiAudioSpeechResponseMetadata withRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
		return this;
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getClass().getName(), getRateLimit());
	}

}
