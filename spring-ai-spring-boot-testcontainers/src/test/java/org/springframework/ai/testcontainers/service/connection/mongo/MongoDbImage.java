package org.springframework.ai.testcontainers.service.connection.mongo;

import org.testcontainers.utility.DockerImageName;

public final class MongoDbImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("mongodb/mongodb-atlas-local:8.0.0");

	private MongoDbImage() {

	}

}
