package org.springframework.ai.chat.memory.cassandra;

import java.time.Duration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

@Testcontainers
class CassandraChatMemoryIT {

	@Container
	static CassandraContainer<?> cassandraContainer = new CassandraContainer<>(CassandraImage.DEFAULT_IMAGE);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(CassandraChatMemoryIT.TestApplication.class);

	@Test
	void ensureBeanGetsCreated() {
		this.contextRunner.run(context -> {
			CassandraChatMemory memory = context.getBean(CassandraChatMemory.class);
			Assertions.assertNotNull(memory);
			memory.conf.checkSchemaValid();
		});
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
	public static class TestApplication {

		@Bean
		public CassandraChatMemory memory(CqlSession cqlSession) {

			var conf = CassandraChatMemoryConfig.builder()
				.withCqlSession(cqlSession)
				.withKeyspaceName("test_" + CassandraChatMemoryConfig.DEFAULT_KEYSPACE_NAME)
				.withAssistantColumnName("a")
				.withUserColumnName("u")
				.withTimeToLive(Duration.ofMinutes(1))
				.build();

			conf.dropKeyspace();
			return CassandraChatMemory.create(conf);
		}

		@Bean
		public CqlSession cqlSession() {
			return new CqlSessionBuilder()

				.addContactPoint(cassandraContainer.getContactPoint())
				.withLocalDatacenter(cassandraContainer.getLocalDatacenter())
				.build();
		}

	}

}
