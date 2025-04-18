package org.springframework.ai.docker.compose.service.connection.chroma;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChromaEnvironmentTests {

	@Test
	void getKeyTokenWhenNoCredential() {
		ChromaEnvironment environment = new ChromaEnvironment(Collections.emptyMap());
		assertThat(environment.getKeyToken()).isNull();
	}

	@Test
	void getKeyTokenFromAuthCredentialsWhenHasCredential() {
		ChromaEnvironment environment = new ChromaEnvironment(Map.of("CHROMA_SERVER_AUTH_CREDENTIALS", "secret"));
		assertThat(environment.getKeyToken()).isEqualTo("secret");
	}

	@Test
	void getKeyTokenFromAuthnCredentialsWhenHasCredential() {
		ChromaEnvironment environment = new ChromaEnvironment(Map.of("CHROMA_SERVER_AUTHN_CREDENTIALS", "secret"));
		assertThat(environment.getKeyToken()).isEqualTo("secret");
	}

}
