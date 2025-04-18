package org.springframework.ai.docker.compose.service.connection.opensearch;

import java.util.List;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class OpenSearchDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<OpenSearchConnectionDetails> {

	private static final int OPENSEARCH_PORT = 9200;

	protected OpenSearchDockerComposeConnectionDetailsFactory() {
		super("opensearchproject/opensearch");
	}

	@Override
	protected OpenSearchConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new OpenSearchDockerComposeConnectionDetails(source.getRunningService());
	}

	static class OpenSearchDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements OpenSearchConnectionDetails {

		private final OpenSearchEnvironment environment;

		private final String uri;

		OpenSearchDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.environment = new OpenSearchEnvironment(service.env());
			this.uri = "http://" + service.host() + ":" + service.ports().get(OPENSEARCH_PORT);
		}

		@Override
		public List<String> getUris() {
			return List.of(this.uri);
		}

		@Override
		public String getUsername() {
			return "admin";
		}

		@Override
		public String getPassword() {
			return this.environment.getPassword();
		}

	}

}
