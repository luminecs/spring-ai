package org.springframework.ai.docker.compose.service.connection.weaviate;

import org.springframework.ai.vectorstore.weaviate.autoconfigure.WeaviateConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class WeaviateDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<WeaviateConnectionDetails> {

	private static final String[] WEAVIATE_IMAGE_NAMES = { "semitechnologies/weaviate",
			"cr.weaviate.io/semitechnologies/weaviate" };

	private static final int WEAVIATE_PORT = 8080;

	protected WeaviateDockerComposeConnectionDetailsFactory() {
		super(WEAVIATE_IMAGE_NAMES);
	}

	@Override
	protected WeaviateConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new WeaviateDockerComposeConnectionDetails(source.getRunningService());
	}

	static class WeaviateDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements WeaviateConnectionDetails {

		private final String host;

		WeaviateDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.host = service.host() + ":" + service.ports().get(WEAVIATE_PORT);
		}

		@Override
		public String getHost() {
			return this.host;
		}

	}

}
