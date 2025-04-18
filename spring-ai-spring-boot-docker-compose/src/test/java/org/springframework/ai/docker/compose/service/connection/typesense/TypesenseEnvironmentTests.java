package org.springframework.ai.docker.compose.service.connection.typesense;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypesenseEnvironmentTests {

	@Test
	void getApiKeyWhenNoApiKey() {
		TypesenseEnvironment environment = new TypesenseEnvironment(Collections.emptyMap());
		assertThat(environment.getApiKey()).isNull();
	}

	@Test
	void getApiKeyWhenHasApiKey() {
		TypesenseEnvironment environment = new TypesenseEnvironment(Map.of("TYPESENSE_API_KEY", "secret"));
		assertThat(environment.getApiKey()).isEqualTo("secret");
	}

}
