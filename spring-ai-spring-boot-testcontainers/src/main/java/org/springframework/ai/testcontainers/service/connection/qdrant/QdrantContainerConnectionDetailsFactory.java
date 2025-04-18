package org.springframework.ai.testcontainers.service.connection.qdrant;

import org.testcontainers.qdrant.QdrantContainer;

import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class QdrantContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<QdrantContainer, QdrantConnectionDetails> {

	@Override
	public QdrantConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<QdrantContainer> source) {
		return new QdrantContainerConnectionDetails(source);
	}

	private static final class QdrantContainerConnectionDetails extends ContainerConnectionDetails<QdrantContainer>
			implements QdrantConnectionDetails {

		private QdrantContainerConnectionDetails(ContainerConnectionSource<QdrantContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return getContainer().getHost();
		}

		@Override
		public int getPort() {
			return getContainer().getMappedPort(6334);
		}

		@Override
		public String getApiKey() {
			return getContainer().getEnvMap().get("QDRANT__SERVICE__API_KEY");
		}

	}

}
