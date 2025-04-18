package org.springframework.ai.azure.openai.metadata;

import java.util.Objects;

import com.azure.ai.openai.models.ImageGenerations;

import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.util.Assert;

public class AzureOpenAiImageResponseMetadata extends ImageResponseMetadata {

	private final Long created;

	protected AzureOpenAiImageResponseMetadata(Long created) {
		this.created = created;
	}

	public static AzureOpenAiImageResponseMetadata from(ImageGenerations openAiImageResponse) {
		Assert.notNull(openAiImageResponse, "OpenAiImageResponse must not be null");
		return new AzureOpenAiImageResponseMetadata(openAiImageResponse.getCreatedAt().toEpochSecond());
	}

	@Override
	public Long getCreated() {
		return this.created;
	}

	@Override
	public String toString() {
		return "AzureOpenAiImageResponseMetadata{" + "created=" + this.created + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AzureOpenAiImageResponseMetadata that)) {
			return false;
		}
		return Objects.equals(this.created, that.created);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.created);
	}

}
