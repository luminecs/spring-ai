package org.springframework.ai.docker.compose.service.connection.mongo;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.docker.compose.service.connection.test.AbstractDockerComposeIT;

import static org.assertj.core.api.Assertions.assertThat;

class MongoDbAtlasLocalDockerComposeConnectionDetailsFactoryIT extends AbstractDockerComposeIT {

	protected MongoDbAtlasLocalDockerComposeConnectionDetailsFactoryIT() {
		super("mongo-compose.yaml", DockerImageName.parse("mongodb/mongodb-atlas-local"));
	}

	@Test
	void runCreatesConnectionDetails() {
		MongoConnectionDetails connectionDetails = run(MongoConnectionDetails.class);
		assertThat(connectionDetails.getConnectionString()).isNotNull();
	}

}
