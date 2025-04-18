package org.springframework.ai.testcontainers.service.connection.opensearch;

import java.util.List;

import org.opensearch.testcontainers.OpensearchContainer;

import org.springframework.ai.vectorstore.opensearch.autoconfigure.OpenSearchConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class OpenSearchContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<OpensearchContainer<?>, OpenSearchConnectionDetails> {

	@Override
	public OpenSearchConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<OpensearchContainer<?>> source) {
		return new OpenSearchContainerConnectionDetails(source);
	}

	private static final class OpenSearchContainerConnectionDetails
			extends ContainerConnectionDetails<OpensearchContainer<?>> implements OpenSearchConnectionDetails {

		private OpenSearchContainerConnectionDetails(ContainerConnectionSource<OpensearchContainer<?>> source) {
			super(source);
		}

		@Override
		public List<String> getUris() {
			return List.of(getContainer().getHttpHostAddress());
		}

		@Override
		public String getUsername() {
			return getContainer().isSecurityEnabled() ? getContainer().getUsername() : null;
		}

		@Override
		public String getPassword() {
			return getContainer().isSecurityEnabled() ? getContainer().getPassword() : null;
		}

	}

}
