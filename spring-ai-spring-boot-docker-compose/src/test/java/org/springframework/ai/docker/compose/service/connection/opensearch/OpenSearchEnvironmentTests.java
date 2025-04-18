package org.springframework.ai.docker.compose.service.connection.opensearch;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenSearchEnvironmentTests {

	@Test
	void getPasswordWhenNoPassword() {
		OpenSearchEnvironment environment = new OpenSearchEnvironment(Collections.emptyMap());
		assertThat(environment.getPassword()).isNull();
	}

	@Test
	void getPasswordWhenHasPassword() {
		OpenSearchEnvironment environment = new OpenSearchEnvironment(
				Map.of("OPENSEARCH_INITIAL_ADMIN_PASSWORD", "secret"));
		assertThat(environment.getPassword()).isEqualTo("secret");
	}

}
