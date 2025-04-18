package org.springframework.ai.testcontainers.service.connection.chroma;

import java.util.Map;

import org.testcontainers.chromadb.ChromaDBContainer;

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class ChromaContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<ChromaDBContainer, ChromaConnectionDetails> {

	@Override
	public ChromaConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<ChromaDBContainer> source) {
		return new ChromaDBContainerConnectionDetails(source);
	}

	private static final class ChromaDBContainerConnectionDetails extends ContainerConnectionDetails<ChromaDBContainer>
			implements ChromaConnectionDetails {

		private static final String CHROMA_SERVER_AUTH_CREDENTIALS = "CHROMA_SERVER_AUTH_CREDENTIALS";

		private static final String CHROMA_SERVER_AUTHN_CREDENTIALS = "CHROMA_SERVER_AUTHN_CREDENTIALS";

		private ChromaDBContainerConnectionDetails(ContainerConnectionSource<ChromaDBContainer> source) {
			super(source);
		}

		@Override
		public String getHost() {
			return "http://%s".formatted(getContainer().getHost());
		}

		@Override
		public int getPort() {
			return getContainer().getMappedPort(8000);
		}

		@Override
		public String getKeyToken() {
			Map<String, String> envVars = getContainer().getEnvMap();
			if (envVars.containsKey(CHROMA_SERVER_AUTH_CREDENTIALS)) {
				return envVars.get(CHROMA_SERVER_AUTH_CREDENTIALS);
			}
			return envVars.get(CHROMA_SERVER_AUTHN_CREDENTIALS);
		}

	}

}
