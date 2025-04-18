package org.springframework.ai.moderation;

import java.util.Objects;

import org.springframework.ai.model.ModelRequest;

public class ModerationPrompt implements ModelRequest<ModerationMessage> {

	private final ModerationMessage message;

	private ModerationOptions moderationModelOptions;

	public ModerationPrompt(ModerationMessage message, ModerationOptions moderationModelOptions) {
		this.message = message;
		this.moderationModelOptions = moderationModelOptions;
	}

	public ModerationPrompt(String instructions, ModerationOptions moderationOptions) {
		this(new ModerationMessage(instructions), moderationOptions);
	}

	public ModerationPrompt(String instructions) {
		this(new ModerationMessage(instructions), ModerationOptionsBuilder.builder().build());
	}

	@Override
	public ModerationMessage getInstructions() {
		return this.message;
	}

	public ModerationOptions getOptions() {
		return this.moderationModelOptions;
	}

	public void setOptions(ModerationOptions moderationModelOptions) {
		this.moderationModelOptions = moderationModelOptions;
	}

	@Override
	public String toString() {
		return "ModerationPrompt{" + "message=" + this.message + ", moderationModelOptions="
				+ this.moderationModelOptions + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModerationPrompt)) {
			return false;
		}
		ModerationPrompt that = (ModerationPrompt) o;
		return Objects.equals(this.message, that.message)
				&& Objects.equals(this.moderationModelOptions, that.moderationModelOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.message, this.moderationModelOptions);
	}

}
