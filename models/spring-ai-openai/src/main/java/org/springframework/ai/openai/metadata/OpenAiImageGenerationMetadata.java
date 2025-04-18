package org.springframework.ai.openai.metadata;

import java.util.Objects;

import org.springframework.ai.image.ImageGenerationMetadata;

public class OpenAiImageGenerationMetadata implements ImageGenerationMetadata {

	private String revisedPrompt;

	public OpenAiImageGenerationMetadata(String revisedPrompt) {
		this.revisedPrompt = revisedPrompt;
	}

	public String getRevisedPrompt() {
		return this.revisedPrompt;
	}

	@Override
	public String toString() {
		return "OpenAiImageGenerationMetadata{" + "revisedPrompt='" + this.revisedPrompt + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OpenAiImageGenerationMetadata that)) {
			return false;
		}
		return Objects.equals(this.revisedPrompt, that.revisedPrompt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.revisedPrompt);
	}

}
