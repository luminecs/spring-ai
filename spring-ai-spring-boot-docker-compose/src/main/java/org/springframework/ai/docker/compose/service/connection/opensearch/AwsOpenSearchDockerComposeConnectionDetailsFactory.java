package org.springframework.ai.docker.compose.service.connection.opensearch;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.AwsOpenSearchConnectionDetails;
import org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class AwsOpenSearchDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<AwsOpenSearchConnectionDetails> {

	private static final int LOCALSTACK_PORT = 4566;

	protected AwsOpenSearchDockerComposeConnectionDetailsFactory() {
		super("localstack/localstack");
	}

	@Override
	protected AwsOpenSearchConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new AwsOpenSearchDockerComposeConnectionDetails(source.getRunningService());
	}

	static class AwsOpenSearchDockerComposeConnectionDetails extends DockerComposeConnectionDetails
			implements AwsOpenSearchConnectionDetails {

		private final AwsOpenSearchEnvironment environment;

		private final int port;

		AwsOpenSearchDockerComposeConnectionDetails(RunningService service) {
			super(service);
			this.environment = new AwsOpenSearchEnvironment(service.env());
			this.port = service.ports().get(LOCALSTACK_PORT);
		}

		@Override
		public String getRegion() {
			return this.environment.getRegion();
		}

		@Override
		public String getAccessKey() {
			return this.environment.getAccessKey();
		}

		@Override
		public String getSecretKey() {
			return this.environment.getSecretKey();
		}

		@Override
		public String getHost(String domainName) {
			return "%s.%s.opensearch.localhost.localstack.cloud:%s".formatted(domainName, this.environment.getRegion(),
					this.port);
		}

	}

}
