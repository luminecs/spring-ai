package org.springframework.ai.vectorstore.neo4j;

import org.testcontainers.utility.DockerImageName;

public final class Neo4jImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("neo4j:5.24");

	private Neo4jImage() {

	}

}
