package org.springframework.ai.testcontainers.service.connection.typesense;

import org.testcontainers.typesense.TypesenseContainer;

import org.springframework.ai.vectorstore.typesense.autoconfigure.TypesenseConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class TypesenseContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<TypesenseContainer, TypesenseConnectionDetails> {

	@Override
	protected TypesenseConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<TypesenseContainer> source) {
		return new TypesenseContainerConnectionDetails(source);
	}

	private static final class TypesenseContainerConnectionDetails
			extends ContainerConnectionDetails<TypesenseContainer> implements TypesenseConnectionDetails {

		private TypesenseContainerConnectionDetails(ContainerConnectionSource<TypesenseContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return getContainer().getHost();
		}

		@Override
		public String getProtocol() {
			return "http";
		}

		@Override
		public int getPort() {
			return Integer.parseInt(getContainer().getHttpPort());
		}

		@Override
		public String getApiKey() {
			return getContainer().getApiKey();
		}

	}

}
