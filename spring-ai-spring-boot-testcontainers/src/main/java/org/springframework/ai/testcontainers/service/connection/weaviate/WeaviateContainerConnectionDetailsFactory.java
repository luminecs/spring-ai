package org.springframework.ai.testcontainers.service.connection.weaviate;

import org.testcontainers.weaviate.WeaviateContainer;

import org.springframework.ai.vectorstore.weaviate.autoconfigure.WeaviateConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class WeaviateContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<WeaviateContainer, WeaviateConnectionDetails> {

	@Override
	public WeaviateConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<WeaviateContainer> source) {
		return new WeaviateContainerConnectionDetails(source);
	}

	private static final class WeaviateContainerConnectionDetails extends ContainerConnectionDetails<WeaviateContainer>
			implements WeaviateConnectionDetails {

		private WeaviateContainerConnectionDetails(ContainerConnectionSource<WeaviateContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return getContainer().getHttpHostAddress();
		}

	}

}
