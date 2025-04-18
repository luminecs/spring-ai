package org.springframework.ai.testcontainers.service.connection.opensearch;

import org.testcontainers.containers.localstack.LocalStackContainer;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.AwsOpenSearchConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class AwsOpenSearchContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<LocalStackContainer, AwsOpenSearchConnectionDetails> {

	@Override
	public AwsOpenSearchConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<LocalStackContainer> source) {
		return new AwsOpenSearchContainerConnectionDetails(source);
	}

	private static final class AwsOpenSearchContainerConnectionDetails
			extends ContainerConnectionDetails<LocalStackContainer> implements AwsOpenSearchConnectionDetails {

		private AwsOpenSearchContainerConnectionDetails(ContainerConnectionSource<LocalStackContainer> source) {
			super(source);
		}

		@Override
		public String getRegion() {
			return getContainer().getRegion();
		}

		@Override
		public String getAccessKey() {
			return getContainer().getAccessKey();
		}

		@Override
		public String getSecretKey() {
			return getContainer().getSecretKey();
		}

		@Override
		public String getHost(String domainName) {
			return "%s.%s.opensearch.localhost.localstack.cloud:%s".formatted(domainName, getContainer().getRegion(),
					getContainer().getMappedPort(4566));
		}

	}

}
