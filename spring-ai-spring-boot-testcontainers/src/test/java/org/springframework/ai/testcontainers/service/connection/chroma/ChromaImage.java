package org.springframework.ai.testcontainers.service.connection.chroma;

import org.testcontainers.utility.DockerImageName;

public final class ChromaImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("ghcr.io/chroma-core/chroma:0.5.20");

	private ChromaImage() {

	}

}
