package org.springframework.ai.azure.openai.metadata;

import org.springframework.ai.audio.transcription.AudioTranscriptionResponseMetadata;
import org.springframework.ai.azure.openai.AzureOpenAiAudioTranscriptionOptions;
import org.springframework.util.Assert;

public class AzureOpenAiAudioTranscriptionResponseMetadata extends AudioTranscriptionResponseMetadata {

	public static final AzureOpenAiAudioTranscriptionResponseMetadata NULL = new AzureOpenAiAudioTranscriptionResponseMetadata() {

	};

	protected static final String AI_METADATA_STRING = "{ @type: %1$s }";

	protected AzureOpenAiAudioTranscriptionResponseMetadata() {
	}

	public static AzureOpenAiAudioTranscriptionResponseMetadata from(
			AzureOpenAiAudioTranscriptionOptions.StructuredResponse result) {
		Assert.notNull(result, "AzureOpenAI Transcription must not be null");
		return new AzureOpenAiAudioTranscriptionResponseMetadata();
	}

	public static AzureOpenAiAudioTranscriptionResponseMetadata from(String result) {
		Assert.notNull(result, "AzureOpenAI Transcription must not be null");
		return new AzureOpenAiAudioTranscriptionResponseMetadata();
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getClass().getName());
	}

}
