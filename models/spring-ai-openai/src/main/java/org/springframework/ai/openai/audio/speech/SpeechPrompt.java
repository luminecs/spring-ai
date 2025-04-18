package org.springframework.ai.openai.audio.speech;

import java.util.Objects;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;

public class SpeechPrompt implements ModelRequest<SpeechMessage> {

	private final SpeechMessage message;

	private OpenAiAudioSpeechOptions speechOptions;

	public SpeechPrompt(String instructions) {
		this(new SpeechMessage(instructions), OpenAiAudioSpeechOptions.builder().build());
	}

	public SpeechPrompt(String instructions, OpenAiAudioSpeechOptions speechOptions) {
		this(new SpeechMessage(instructions), speechOptions);
	}

	public SpeechPrompt(SpeechMessage speechMessage) {
		this(speechMessage, OpenAiAudioSpeechOptions.builder().build());
	}

	public SpeechPrompt(SpeechMessage speechMessage, OpenAiAudioSpeechOptions speechOptions) {
		this.message = speechMessage;
		this.speechOptions = speechOptions;
	}

	@Override
	public SpeechMessage getInstructions() {
		return this.message;
	}

	@Override
	public ModelOptions getOptions() {
		return this.speechOptions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpeechPrompt that)) {
			return false;
		}
		return Objects.equals(this.speechOptions, that.speechOptions) && Objects.equals(this.message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.speechOptions, this.message);
	}

}
