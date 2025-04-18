package org.springframework.ai.docker.compose.service.connection.qdrant;

import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class QdrantDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<QdrantConnectionDetails> {

	private static final int QDRANT_GRPC_PORT = 6334;

	protected QdrantDockerComposeConnectionDetailsFactory() {
		super("qdrant/qdrant");
	}

	@Override
	protected QdrantConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new QdrantDockerComposeConnectionDetails(source.getRunningService());
	}

	static class QdrantDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements QdrantConnectionDetails {

		private final QdrantEnvironment environment;

		private final String host;

		private final int port;

		QdrantDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.environment = new QdrantEnvironment(service.env());
			this.host = service.host();
			this.port = service.ports().get(QDRANT_GRPC_PORT);
		}

		@Override
		public String getHost() {
			return this.host;
		}

		@Override
		public int getPort() {
			return this.port;
		}

		@Override
		public String getApiKey() {
			return this.environment.getApiKey();
		}

	}

}
