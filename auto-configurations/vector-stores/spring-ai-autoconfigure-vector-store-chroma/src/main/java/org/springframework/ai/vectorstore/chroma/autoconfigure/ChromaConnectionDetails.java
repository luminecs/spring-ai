package org.springframework.ai.vectorstore.chroma.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface ChromaConnectionDetails extends ConnectionDetails {

	String getHost();

	int getPort();

	String getKeyToken();

}
