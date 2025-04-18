package org.springframework.ai.docker.compose.service.connection.qdrant;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class QdrantDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	QdrantDockerComposeConnectionDetailsFactoryIT() {
		super("qdrant-compose.yaml", DockerImageName.parse("qdrant/qdrant"));
	}

	@Test
	void runCreatesConnectionDetails() {
		QdrantConnectionDetails connectionDetails = run(QdrantConnectionDetails.class);
		assertThat(connectionDetails.getHost()).isNotNull();
		assertThat(connectionDetails.getPort()).isGreaterThan(0);
		assertThat(connectionDetails.getApiKey()).isEqualTo("springai");
	}

}
