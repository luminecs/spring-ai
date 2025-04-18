package org.springframework.ai.ollama;

import org.testcontainers.utility.DockerImageName;

public final class OllamaImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("ollama/ollama:0.5.2");

	private OllamaImage() {

	}

}
