package org.springframework.ai.model.ollama.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface OllamaConnectionDetails extends ConnectionDetails {

	String getBaseUrl();

}
