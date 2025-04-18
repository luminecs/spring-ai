package org.springframework.ai.docker.compose.service.connection.opensearch;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class OpenSearchDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	OpenSearchDockerComposeConnectionDetailsFactoryIT() {
		super("opensearch-compose.yaml", DockerImageName.parse("opensearchproject/opensearch"));
	}

	@Test
	void runCreatesConnectionDetails() {
		OpenSearchConnectionDetails connectionDetails = run(OpenSearchConnectionDetails.class);
		assertThat(connectionDetails.getUris()).isNotNull();
		assertThat(connectionDetails.getUsername()).isEqualTo("admin");
		assertThat(connectionDetails.getPassword()).isEqualTo("D3v3l0p-ment");
	}

}
