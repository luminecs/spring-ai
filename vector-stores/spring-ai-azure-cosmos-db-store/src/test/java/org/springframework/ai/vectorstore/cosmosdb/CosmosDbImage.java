package org.springframework.ai.vectorstore.cosmosdb;

import org.testcontainers.utility.DockerImageName;

public final class CosmosDbImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName
		.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest");

	private CosmosDbImage() {

	}

}
