package org.springframework.ai.docker.compose.service.connection.chroma;

import java.util.Map;

class ChromaEnvironment {

	private static final String CHROMA_SERVER_AUTH_CREDENTIALS = "CHROMA_SERVER_AUTH_CREDENTIALS";

	private static final String CHROMA_SERVER_AUTHN_CREDENTIALS = "CHROMA_SERVER_AUTHN_CREDENTIALS";

	private final String keyToken;

	ChromaEnvironment(Map<String, String> env) {
		if (env.containsKey(CHROMA_SERVER_AUTH_CREDENTIALS)) {
			this.keyToken = env.get(CHROMA_SERVER_AUTH_CREDENTIALS);
			return;
		}
		this.keyToken = env.get(CHROMA_SERVER_AUTHN_CREDENTIALS);
	}

	public String getKeyToken() {
		return this.keyToken;
	}

}
