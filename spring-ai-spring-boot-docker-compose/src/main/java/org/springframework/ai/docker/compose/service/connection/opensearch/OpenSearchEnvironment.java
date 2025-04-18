package org.springframework.ai.docker.compose.service.connection.opensearch;

import java.util.Map;

class OpenSearchEnvironment {

	private final String password;

	OpenSearchEnvironment(Map<String, String> env) {
		this.password = env.get("OPENSEARCH_INITIAL_ADMIN_PASSWORD");
	}

	String getPassword() {
		return this.password;
	}

}
