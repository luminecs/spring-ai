package org.springframework.ai.vectorstore.typesense.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface TypesenseConnectionDetails extends ConnectionDetails {

	String getHost();

	String getProtocol();

	int getPort();

	String getApiKey();

}
