package org.springframework.ai.vectorstore.opensearch.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface AwsOpenSearchConnectionDetails extends ConnectionDetails {

	String getRegion();

	String getAccessKey();

	String getSecretKey();

	String getHost(String domainName);

}
