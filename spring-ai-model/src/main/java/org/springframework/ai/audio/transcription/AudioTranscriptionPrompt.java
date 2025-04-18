package org.springframework.ai.audio.transcription;

import org.springframework.ai.model.ModelRequest;
import org.springframework.core.io.Resource;

public class AudioTranscriptionPrompt implements ModelRequest<Resource> {

	private final Resource audioResource;

	private AudioTranscriptionOptions modelOptions;

	public AudioTranscriptionPrompt(Resource audioResource) {
		this.audioResource = audioResource;
	}

	public AudioTranscriptionPrompt(Resource audioResource, AudioTranscriptionOptions modelOptions) {
		this.audioResource = audioResource;
		this.modelOptions = modelOptions;
	}

	@Override
	public Resource getInstructions() {
		return this.audioResource;
	}

	@Override
	public AudioTranscriptionOptions getOptions() {
		return this.modelOptions;
	}

}
