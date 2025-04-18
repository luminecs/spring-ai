package org.springframework.ai.docker.compose.service.connection.typesense;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.vectorstore.typesense.autoconfigure.TypesenseConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class TypesenseDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	TypesenseDockerComposeConnectionDetailsFactoryIT() {
		super("typesense-compose.yaml", DockerImageName.parse("typesense/typesense:26.0"));
	}

	@Test
	void runCreatesConnectionDetails() {
		TypesenseConnectionDetails connectionDetails = run(TypesenseConnectionDetails.class);
		assertThat(connectionDetails.getHost()).isNotNull();
		assertThat(connectionDetails.getPort()).isGreaterThan(0);
		assertThat(connectionDetails.getProtocol()).isEqualTo("http");
		assertThat(connectionDetails.getApiKey()).isEqualTo("secret");
	}

}
