package org.springframework.ai.vectorstore.elasticsearch;

import org.testcontainers.utility.DockerImageName;

public final class ElasticsearchImage {

	public static final DockerImageName DEFAULT_IMAGE = DockerImageName
		.parse("docker.elastic.co/elasticsearch/elasticsearch:8.16.1");

	private ElasticsearchImage() {

	}

}
