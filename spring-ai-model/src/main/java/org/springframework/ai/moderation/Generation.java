package org.springframework.ai.moderation;

import org.springframework.ai.model.ModelResult;
import org.springframework.lang.Nullable;

public class Generation implements ModelResult<Moderation> {

	private ModerationGenerationMetadata moderationGenerationMetadata;

	private Moderation moderation;

	public Generation() {

	}

	public Generation(Moderation moderation) {
		this.moderation = moderation;
	}

	public Generation(Moderation moderation, ModerationGenerationMetadata moderationGenerationMetadata) {
		this.moderation = moderation;
		this.moderationGenerationMetadata = moderationGenerationMetadata;
	}

	public Generation generationMetadata(@Nullable ModerationGenerationMetadata moderationGenerationMetadata) {
		this.moderationGenerationMetadata = moderationGenerationMetadata;
		return this;
	}

	@Override
	public Moderation getOutput() {
		return this.moderation;
	}

	@Override
	public ModerationGenerationMetadata getMetadata() {
		return this.moderationGenerationMetadata;
	}

	@Override
	public String toString() {
		return "Generation{" + "moderationGenerationMetadata=" + this.moderationGenerationMetadata + ", moderation="
				+ this.moderation + '}';
	}

}
