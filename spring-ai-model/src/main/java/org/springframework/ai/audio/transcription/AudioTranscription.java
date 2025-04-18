package org.springframework.ai.audio.transcription;

import java.util.Objects;

import org.springframework.ai.model.ModelResult;
import org.springframework.lang.Nullable;

public class AudioTranscription implements ModelResult<String> {

	private final String text;

	private AudioTranscriptionMetadata transcriptionMetadata;

	public AudioTranscription(String text) {
		this.text = text;
	}

	@Override
	public String getOutput() {
		return this.text;
	}

	@Override
	public AudioTranscriptionMetadata getMetadata() {
		return this.transcriptionMetadata != null ? this.transcriptionMetadata : AudioTranscriptionMetadata.NULL;
	}

	public AudioTranscription withTranscriptionMetadata(@Nullable AudioTranscriptionMetadata transcriptionMetadata) {
		this.transcriptionMetadata = transcriptionMetadata;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AudioTranscription that)) {
			return false;
		}
		return Objects.equals(this.text, that.text)
				&& Objects.equals(this.transcriptionMetadata, that.transcriptionMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.text, this.transcriptionMetadata);
	}

	@Override
	public String toString() {
		return "Transcript{" + "text=" + this.text + ", transcriptionMetadata=" + this.transcriptionMetadata + '}';
	}

}
