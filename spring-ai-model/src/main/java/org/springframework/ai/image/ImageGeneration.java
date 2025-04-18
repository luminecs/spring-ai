package org.springframework.ai.image;

import org.springframework.ai.model.ModelResult;

public class ImageGeneration implements ModelResult<Image> {

	private ImageGenerationMetadata imageGenerationMetadata;

	private Image image;

	public ImageGeneration(Image image) {
		this.image = image;
	}

	public ImageGeneration(Image image, ImageGenerationMetadata imageGenerationMetadata) {
		this.image = image;
		this.imageGenerationMetadata = imageGenerationMetadata;
	}

	@Override
	public Image getOutput() {
		return this.image;
	}

	@Override
	public ImageGenerationMetadata getMetadata() {
		return this.imageGenerationMetadata;
	}

	@Override
	public String toString() {
		return "ImageGeneration{" + "imageGenerationMetadata=" + this.imageGenerationMetadata + ", image=" + this.image
				+ '}';
	}

}
