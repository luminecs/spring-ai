package org.springframework.ai.azure.openai.metadata;

import java.util.Objects;

import org.springframework.ai.image.ImageGenerationMetadata;

public class AzureOpenAiImageGenerationMetadata implements ImageGenerationMetadata {

	private final String revisedPrompt;

	public AzureOpenAiImageGenerationMetadata(String revisedPrompt) {
		this.revisedPrompt = revisedPrompt;
	}

	public String getRevisedPrompt() {
		return this.revisedPrompt;
	}

	public String toString() {
		return "AzureOpenAiImageGenerationMetadata{" + "revisedPrompt='" + this.revisedPrompt + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AzureOpenAiImageGenerationMetadata that)) {
			return false;
		}
		return Objects.equals(this.revisedPrompt, that.revisedPrompt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.revisedPrompt);
	}

}
