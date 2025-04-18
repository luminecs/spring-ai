package org.springframework.ai.testcontainers.service.connection.milvus;

import org.testcontainers.milvus.MilvusContainer;

import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusServiceClientConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class MilvusContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<MilvusContainer, MilvusServiceClientConnectionDetails> {

	@Override
	public MilvusServiceClientConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<MilvusContainer> source) {
		return new MilvusContainerConnectionDetails(source);
	}

	private static final class MilvusContainerConnectionDetails extends ContainerConnectionDetails<MilvusContainer>
			implements MilvusServiceClientConnectionDetails {

		private MilvusContainerConnectionDetails(ContainerConnectionSource<MilvusContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return getContainer().getHost();
		}

		@Override
		public int getPort() {
			return getContainer().getMappedPort(19530);
		}

	}

}
