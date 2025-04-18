package org.springframework.ai.testcontainers.service.connection.opensearch;

import org.testcontainers.utility.DockerImageName;

public final class OpenSearchImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("opensearchproject/opensearch:2.17.1");

	private OpenSearchImage() {

	}

}
