package org.springframework.ai.vectorstore.pgvector;

import org.testcontainers.utility.DockerImageName;

public final class PgVectorImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("pgvector/pgvector:pg17");

	private PgVectorImage() {

	}

}
