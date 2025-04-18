package org.springframework.ai.audio.transcription;

import org.springframework.ai.model.ResultMetadata;

public interface AudioTranscriptionMetadata extends ResultMetadata {

	AudioTranscriptionMetadata NULL = AudioTranscriptionMetadata.create();

	static AudioTranscriptionMetadata create() {
		return new AudioTranscriptionMetadata() {

		};
	}

}
