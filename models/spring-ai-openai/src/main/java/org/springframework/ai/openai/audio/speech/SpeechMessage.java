package org.springframework.ai.openai.audio.speech;

import java.util.Objects;

public class SpeechMessage {

	private String text;

	public SpeechMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SpeechMessage that)) {
			return false;
		}
		return Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.text);
	}

}
