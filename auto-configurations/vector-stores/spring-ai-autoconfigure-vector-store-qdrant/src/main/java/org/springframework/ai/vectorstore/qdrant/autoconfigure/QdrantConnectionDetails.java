package org.springframework.ai.vectorstore.qdrant.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface QdrantConnectionDetails extends ConnectionDetails {

	String getHost();

	int getPort();

	String getApiKey();

}
