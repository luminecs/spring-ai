package org.springframework.ai.vectorstore.gemfire.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface GemFireConnectionDetails extends ConnectionDetails {

	String getHost();

	int getPort();

}
