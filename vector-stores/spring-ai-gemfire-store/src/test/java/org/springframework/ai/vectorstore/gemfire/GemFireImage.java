package org.springframework.ai.vectorstore.gemfire;

import org.testcontainers.utility.DockerImageName;

public final class GemFireImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("gemfire/gemfire-all:10.1-jdk17");

	private GemFireImage() {

	}

}
