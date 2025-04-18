package org.springframework.ai.audio.transcription;

import java.util.List;

import org.springframework.ai.model.ModelResponse;

public class AudioTranscriptionResponse implements ModelResponse<AudioTranscription> {

	private final AudioTranscription transcript;

	private final AudioTranscriptionResponseMetadata transcriptionResponseMetadata;

	public AudioTranscriptionResponse(AudioTranscription transcript) {
		this(transcript, new AudioTranscriptionResponseMetadata());
	}

	public AudioTranscriptionResponse(AudioTranscription transcript,
			AudioTranscriptionResponseMetadata transcriptionResponseMetadata) {
		this.transcript = transcript;
		this.transcriptionResponseMetadata = transcriptionResponseMetadata;
	}

	@Override
	public AudioTranscription getResult() {
		return this.transcript;
	}

	@Override
	public List<AudioTranscription> getResults() {
		return List.of(this.transcript);
	}

	@Override
	public AudioTranscriptionResponseMetadata getMetadata() {
		return this.transcriptionResponseMetadata;
	}

}
