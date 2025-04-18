package org.springframework.ai.vectorstore.opensearch.autoconfigure;

import java.util.List;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface OpenSearchConnectionDetails extends ConnectionDetails {

	List<String> getUris();

	String getUsername();

	String getPassword();

}
