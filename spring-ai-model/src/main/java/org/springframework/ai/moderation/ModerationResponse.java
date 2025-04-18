package org.springframework.ai.moderation;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.model.ModelResponse;

public class ModerationResponse implements ModelResponse<Generation> {

	private final ModerationResponseMetadata moderationResponseMetadata;

	private final Generation generations;

	public ModerationResponse(Generation generations) {
		this(generations, new ModerationResponseMetadata());
	}

	public ModerationResponse(Generation generations, ModerationResponseMetadata moderationResponseMetadata) {
		this.moderationResponseMetadata = moderationResponseMetadata;
		this.generations = generations;
	}

	@Override
	public Generation getResult() {
		return this.generations;
	}

	@Override
	public List<Generation> getResults() {
		return List.of(this.generations);
	}

	@Override
	public ModerationResponseMetadata getMetadata() {
		return this.moderationResponseMetadata;
	}

	@Override
	public String toString() {
		return "ModerationResponse{" + "moderationResponseMetadata=" + this.moderationResponseMetadata
				+ ", generations=" + this.generations + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModerationResponse that)) {
			return false;
		}
		return Objects.equals(this.moderationResponseMetadata, that.moderationResponseMetadata)
				&& Objects.equals(this.generations, that.generations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.moderationResponseMetadata, this.generations);
	}

}
