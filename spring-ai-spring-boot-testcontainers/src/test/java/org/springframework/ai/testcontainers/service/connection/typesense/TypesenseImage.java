package org.springframework.ai.testcontainers.service.connection.typesense;

import org.testcontainers.utility.DockerImageName;

public final class TypesenseImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("typesense/typesense:27.1");

	private TypesenseImage() {

	}

}
