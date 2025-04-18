package org.springframework.ai.openai.metadata.audio;

import org.springframework.ai.model.ResultMetadata;

public interface OpenAiAudioSpeechMetadata extends ResultMetadata {

	OpenAiAudioSpeechMetadata NULL = OpenAiAudioSpeechMetadata.create();

	static OpenAiAudioSpeechMetadata create() {
		return new OpenAiAudioSpeechMetadata() {

		};
	}

}
