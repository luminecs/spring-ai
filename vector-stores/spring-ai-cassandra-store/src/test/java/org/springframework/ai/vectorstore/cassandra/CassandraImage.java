package org.springframework.ai.vectorstore.cassandra;

import org.testcontainers.utility.DockerImageName;

public final class CassandraImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("cassandra:5.0");

	private CassandraImage() {

	}

}
