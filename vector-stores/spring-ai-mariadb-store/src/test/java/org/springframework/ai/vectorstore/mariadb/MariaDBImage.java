package org.springframework.ai.vectorstore.mariadb;

import org.testcontainers.utility.DockerImageName;

public final class MariaDBImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("mariadb:11.7-rc");

	private MariaDBImage() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

}
