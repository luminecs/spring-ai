package org.springframework.ai.image;

import org.springframework.ai.model.MutableResponseMetadata;

public class ImageResponseMetadata extends MutableResponseMetadata {

	private final Long created;

	public ImageResponseMetadata() {
		this(System.currentTimeMillis());
	}

	public ImageResponseMetadata(Long created) {
		this.created = created;
	}

	public Long getCreated() {
		return this.created;
	}

}
