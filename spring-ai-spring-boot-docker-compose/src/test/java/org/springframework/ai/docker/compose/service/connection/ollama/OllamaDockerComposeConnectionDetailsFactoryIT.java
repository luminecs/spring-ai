package org.springframework.ai.docker.compose.service.connection.ollama;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	OllamaDockerComposeConnectionDetailsFactoryIT() {
		super("ollama-compose.yaml", DockerImageName.parse("ollama/ollama"));
	}

	@Test
	void runCreatesConnectionDetails() {
		OllamaConnectionDetails connectionDetails = run(OllamaConnectionDetails.class);
		assertThat(connectionDetails.getBaseUrl()).startsWith("http://");
	}

}
