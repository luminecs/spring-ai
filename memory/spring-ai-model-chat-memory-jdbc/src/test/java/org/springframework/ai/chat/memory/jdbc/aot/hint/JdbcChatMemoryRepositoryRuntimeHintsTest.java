package org.springframework.ai.chat.memory.jdbc.aot.hint;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcChatMemoryRepositoryRuntimeHintsTest {

	private final RuntimeHints hints = new RuntimeHints();

	private final JdbcChatMemoryRepositoryRuntimeHints jdbcChatMemoryRepositoryRuntimeHints = new JdbcChatMemoryRepositoryRuntimeHints();

	@Test
	void aotFactoriesContainsRegistrar() {
		var match = SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories")
			.load(RuntimeHintsRegistrar.class)
			.stream()
			.anyMatch(registrar -> registrar instanceof JdbcChatMemoryRepositoryRuntimeHints);

		assertThat(match).isTrue();
	}

	@ParameterizedTest
	@MethodSource("getSchemaFileNames")
	void jdbcSchemasHasHints(String schemaFileName) {
		this.jdbcChatMemoryRepositoryRuntimeHints.registerHints(this.hints, getClass().getClassLoader());

		var predicate = RuntimeHintsPredicates.resource()
			.forResource("org/springframework/ai/chat/memory/jdbc/" + schemaFileName);

		assertThat(predicate).accepts(this.hints);
	}

	@Test
	void dataSourceHasHints() {
		this.jdbcChatMemoryRepositoryRuntimeHints.registerHints(this.hints, getClass().getClassLoader());

		assertThat(RuntimeHintsPredicates.reflection().onType(DataSource.class)).accepts(this.hints);
	}

	private static Stream<String> getSchemaFileNames() throws IOException {
		var resources = new PathMatchingResourcePatternResolver()
			.getResources("classpath*:org/springframework/ai/chat/memory/jdbc/schema-*.sql");

		return Arrays.stream(resources).map(Resource::getFilename);
	}

}
