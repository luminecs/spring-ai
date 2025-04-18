package org.springframework.ai.vectorstore.milvus;

import org.testcontainers.utility.DockerImageName;

public final class MilvusImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("milvusdb/milvus:v2.5.4");

	private MilvusImage() {

	}

}
