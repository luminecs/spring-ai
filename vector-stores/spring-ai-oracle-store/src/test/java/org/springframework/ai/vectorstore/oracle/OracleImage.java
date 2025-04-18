package org.springframework.ai.vectorstore.oracle;

import org.testcontainers.utility.DockerImageName;

public final class OracleImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("gvenzl/oracle-free:23-slim");

	private OracleImage() {

	}

}
