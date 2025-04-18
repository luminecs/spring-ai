package org.springframework.ai.moderation;

import java.util.Objects;

public class ModerationMessage {

	private String text;

	public ModerationMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "ModerationMessage{" + "text='" + this.text + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModerationMessage)) {
			return false;
		}
		ModerationMessage that = (ModerationMessage) o;
		return Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.text);
	}

}
