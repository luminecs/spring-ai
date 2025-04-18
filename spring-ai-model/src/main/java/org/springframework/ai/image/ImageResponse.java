package org.springframework.ai.image;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.model.ModelResponse;
import org.springframework.util.CollectionUtils;

public class ImageResponse implements ModelResponse<ImageGeneration> {

	private final ImageResponseMetadata imageResponseMetadata;

	private final List<ImageGeneration> imageGenerations;

	public ImageResponse(List<ImageGeneration> generations) {
		this(generations, new ImageResponseMetadata());
	}

	public ImageResponse(List<ImageGeneration> generations, ImageResponseMetadata imageResponseMetadata) {
		this.imageResponseMetadata = imageResponseMetadata;
		this.imageGenerations = List.copyOf(generations);
	}

	@Override
	public List<ImageGeneration> getResults() {
		return this.imageGenerations;
	}

	@Override
	public ImageGeneration getResult() {
		if (CollectionUtils.isEmpty(this.imageGenerations)) {
			return null;
		}
		return this.imageGenerations.get(0);
	}

	@Override
	public ImageResponseMetadata getMetadata() {
		return this.imageResponseMetadata;
	}

	@Override
	public String toString() {
		return "ImageResponse [" + "imageResponseMetadata=" + this.imageResponseMetadata + ", imageGenerations="
				+ this.imageGenerations + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ImageResponse that)) {
			return false;
		}
		return Objects.equals(this.imageResponseMetadata, that.imageResponseMetadata)
				&& Objects.equals(this.imageGenerations, that.imageGenerations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.imageResponseMetadata, this.imageGenerations);
	}

}
