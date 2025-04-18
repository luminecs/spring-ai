package org.springframework.ai.docker.compose.service.connection.chroma;

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class ChromaDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<ChromaConnectionDetails> {

	private static final String[] CHROMA_IMAGE_NAMES = { "chromadb/chroma", "ghcr.io/chroma-core/chroma" };

	private static final int CHROMA_PORT = 8000;

	protected ChromaDockerComposeConnectionDetailsFactory() {
		super(CHROMA_IMAGE_NAMES);
	}

	@Override
	protected ChromaConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new ChromaDockerComposeConnectionDetails(source.getRunningService());
	}

	static class ChromaDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements ChromaConnectionDetails {

		private final ChromaEnvironment environment;

		private final String host;

		private final int port;

		ChromaDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.environment = new ChromaEnvironment(service.env());
			this.host = service.host();
			this.port = service.ports().get(CHROMA_PORT);
		}

		@Override
		public String getHost() {
			return "http://%s".formatted(this.host);
		}

		@Override
		public int getPort() {
			return this.port;
		}

		@Override
		public String getKeyToken() {
			return this.environment.getKeyToken();
		}

	}

}
