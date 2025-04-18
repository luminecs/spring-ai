package org.springframework.ai.stabilityai;

import java.util.Objects;

import org.springframework.ai.image.ImageGenerationMetadata;

public class StabilityAiImageGenerationMetadata implements ImageGenerationMetadata {

	private String finishReason;

	private Long seed;

	public StabilityAiImageGenerationMetadata(String finishReason, Long seed) {
		this.finishReason = finishReason;
		this.seed = seed;
	}

	public String getFinishReason() {
		return this.finishReason;
	}

	public Long getSeed() {
		return this.seed;
	}

	@Override
	public String toString() {
		return "StabilityAiImageGenerationMetadata{" + "finishReason='" + this.finishReason + '\'' + ", seed="
				+ this.seed + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StabilityAiImageGenerationMetadata that)) {
			return false;
		}
		return Objects.equals(this.finishReason, that.finishReason) && Objects.equals(this.seed, that.seed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.finishReason, this.seed);
	}

}
