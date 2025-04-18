package org.springframework.ai.testcontainers.service.connection.milvus;

import org.testcontainers.utility.DockerImageName;

public final class MilvusImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("milvusdb/milvus:v2.4.9");

	private MilvusImage() {

	}

}
