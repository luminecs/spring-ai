package org.springframework.ai.docker.compose.service.connection.opensearch;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.AwsOpenSearchConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class AwsOpenSearchDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	AwsOpenSearchDockerComposeConnectionDetailsFactoryIT() {
		super("localstack-compose.yaml", DockerImageName.parse("localstack/localstack:3.5.0"));
	}

	@Test
	void runCreatesConnectionDetails() {
		AwsOpenSearchConnectionDetails connectionDetails = run(AwsOpenSearchConnectionDetails.class);
		assertThat(connectionDetails.getAccessKey()).isEqualTo("test");
		assertThat(connectionDetails.getSecretKey()).isEqualTo("test");
		assertThat(connectionDetails.getRegion()).isEqualTo("us-east-1");
	}

}
