package org.springframework.ai.docker.compose.service.connection.typesense;

import org.springframework.ai.vectorstore.typesense.autoconfigure.TypesenseConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

public class TypesenseDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<TypesenseConnectionDetails> {

	private static final int TYPESENSE_PORT = 8108;

	protected TypesenseDockerComposeConnectionDetailsFactory() {
		super("typesense/typesense");
	}

	@Override
	protected TypesenseConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new TypesenseComposeConnectionDetails(source.getRunningService());
	}

	static class TypesenseComposeConnectionDetails extends DockerComposeConnectionDetails
			implements TypesenseConnectionDetails {

		private final TypesenseEnvironment environment;

		private final String host;

		private final int port;

		TypesenseComposeConnectionDetails(RunningService service) {
			super(service);
			this.environment = new TypesenseEnvironment(service.env());
			this.host = service.host();
			this.port = service.ports().get(TYPESENSE_PORT);
		}

		@Override
		public String getHost() {
			return this.host;
		}

		@Override
		public String getProtocol() {
			return "http";
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
