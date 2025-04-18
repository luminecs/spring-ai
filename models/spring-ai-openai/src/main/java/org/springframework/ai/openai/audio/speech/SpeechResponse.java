package org.springframework.ai.openai.audio.speech;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;

public class SpeechResponse implements ModelResponse<Speech> {

	private final Speech speech;

	private final OpenAiAudioSpeechResponseMetadata speechResponseMetadata;

	public SpeechResponse(Speech speech) {
		this(speech, OpenAiAudioSpeechResponseMetadata.NULL);
	}

	public SpeechResponse(Speech speech, OpenAiAudioSpeechResponseMetadata speechResponseMetadata) {
		this.speech = speech;
		this.speechResponseMetadata = speechResponseMetadata;
	}

	@Override
	public Speech getResult() {
		return this.speech;
	}

	@Override
	public List<Speech> getResults() {
		return Collections.singletonList(this.speech);
	}

	@Override
	public OpenAiAudioSpeechResponseMetadata getMetadata() {
		return this.speechResponseMetadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpeechResponse that)) {
			return false;
		}
		return Objects.equals(this.speech, that.speech)
				&& Objects.equals(this.speechResponseMetadata, that.speechResponseMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.speech, this.speechResponseMetadata);
	}

}
