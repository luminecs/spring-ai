package org.springframework.ai.image;

import org.springframework.ai.model.Model;

@FunctionalInterface
public interface ImageModel extends Model<ImagePrompt, ImageResponse> {

	ImageResponse call(ImagePrompt request);

}
