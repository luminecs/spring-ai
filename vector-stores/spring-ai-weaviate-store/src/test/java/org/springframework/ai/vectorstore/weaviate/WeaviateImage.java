package org.springframework.ai.vectorstore.weaviate;

import org.testcontainers.utility.DockerImageName;

public final class WeaviateImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("semitechnologies/weaviate:1.25.9");

	private WeaviateImage() {

	}

}
