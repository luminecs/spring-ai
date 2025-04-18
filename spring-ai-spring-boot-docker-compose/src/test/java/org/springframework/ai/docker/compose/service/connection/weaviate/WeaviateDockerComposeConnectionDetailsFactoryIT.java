package org.springframework.ai.docker.compose.service.connection.weaviate;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.vectorstore.weaviate.autoconfigure.WeaviateConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class WeaviateDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	WeaviateDockerComposeConnectionDetailsFactoryIT() {
		super("weaviate-compose.yaml", DockerImageName.parse("semitechnologies/weaviate"));
	}

	@Test
	void runCreatesConnectionDetails() {
		WeaviateConnectionDetails connectionDetails = run(WeaviateConnectionDetails.class);
		assertThat(connectionDetails.getHost()).isNotNull();
	}

}
