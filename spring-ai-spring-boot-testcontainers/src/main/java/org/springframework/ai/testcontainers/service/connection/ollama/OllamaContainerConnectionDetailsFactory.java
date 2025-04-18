package org.springframework.ai.testcontainers.service.connection.ollama;

import org.testcontainers.ollama.OllamaContainer;

import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class OllamaContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<OllamaContainer, OllamaConnectionDetails> {

	@Override
	public OllamaConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<OllamaContainer> source) {
		return new OllamaContainerConnectionDetails(source);
	}

	private static final class OllamaContainerConnectionDetails extends ContainerConnectionDetails<OllamaContainer>
			implements OllamaConnectionDetails {

		private OllamaContainerConnectionDetails(ContainerConnectionSource<OllamaContainer> source) {
			super(source);
		}

		@Override
		public String getBaseUrl() {
			return getContainer().getEndpoint();
		}

	}

}
