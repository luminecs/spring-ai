package org.springframework.ai.docker.compose.service.connection.ollama;

import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class OllamaDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<OllamaConnectionDetails> {

	private static final int OLLAMA_PORT = 11434;

	protected OllamaDockerComposeConnectionDetailsFactory() {
		super("ollama/ollama");
	}

	@Override
	protected OllamaConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new OllamaDockerComposeConnectionDetails(source.getRunningService());
	}

	static class OllamaDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements OllamaConnectionDetails {

		private final String baseUrl;

		OllamaDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.baseUrl = "http://" + service.host() + ":" + service.ports().get(OLLAMA_PORT);
		}

		@Override
		public String getBaseUrl() {
			return this.baseUrl;
		}

	}

}
