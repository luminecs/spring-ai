package org.springframework.ai.vectorstore.milvus.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface MilvusServiceClientConnectionDetails extends ConnectionDetails {

	String getHost();

	int getPort();

}
