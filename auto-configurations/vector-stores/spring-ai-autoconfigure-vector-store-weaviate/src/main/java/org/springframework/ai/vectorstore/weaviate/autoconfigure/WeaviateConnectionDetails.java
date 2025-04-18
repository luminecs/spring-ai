package org.springframework.ai.vectorstore.weaviate.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface WeaviateConnectionDetails extends ConnectionDetails {

	String getHost();

}
