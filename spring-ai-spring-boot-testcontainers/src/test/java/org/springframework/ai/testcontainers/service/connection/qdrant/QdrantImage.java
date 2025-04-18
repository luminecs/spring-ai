package org.springframework.ai.testcontainers.service.connection.qdrant;

import org.testcontainers.utility.DockerImageName;

public final class QdrantImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("qdrant/qdrant:v1.9.7");

	private QdrantImage() {

	}

}
