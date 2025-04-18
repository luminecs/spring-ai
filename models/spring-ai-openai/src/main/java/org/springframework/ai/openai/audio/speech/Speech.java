package org.springframework.ai.openai.audio.speech;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechMetadata;
import org.springframework.lang.Nullable;

public class Speech implements ModelResult<byte[]> {

	private final byte[] audio;

	private OpenAiAudioSpeechMetadata speechMetadata;

	public Speech(byte[] audio) {
		this.audio = audio;
	}

	@Override
	public byte[] getOutput() {
		return this.audio;
	}

	@Override
	public OpenAiAudioSpeechMetadata getMetadata() {
		return this.speechMetadata != null ? this.speechMetadata : OpenAiAudioSpeechMetadata.NULL;
	}

	public Speech withSpeechMetadata(@Nullable OpenAiAudioSpeechMetadata speechMetadata) {
		this.speechMetadata = speechMetadata;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Speech that)) {
			return false;
		}
		return Arrays.equals(this.audio, that.audio) && Objects.equals(this.speechMetadata, that.speechMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(this.audio), this.speechMetadata);
	}

	@Override
	public String toString() {
		return "Speech{" + "text=" + this.audio + ", speechMetadata=" + this.speechMetadata + '}';
	}

}
