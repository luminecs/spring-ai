package org.springframework.ai.model.chat.memory.cassandra.autoconfigure;

import com.datastax.oss.driver.api.core.CqlSession;

import org.springframework.ai.chat.memory.cassandra.CassandraChatMemory;
import org.springframework.ai.chat.memory.cassandra.CassandraChatMemoryConfig;
import org.springframework.ai.model.chat.memory.autoconfigure.ChatMemoryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = CassandraAutoConfiguration.class, before = ChatMemoryAutoConfiguration.class)
@ConditionalOnClass({ CassandraChatMemory.class, CqlSession.class })
@EnableConfigurationProperties(CassandraChatMemoryProperties.class)
public class CassandraChatMemoryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public CassandraChatMemory chatMemory(CassandraChatMemoryProperties properties, CqlSession cqlSession) {

		var builder = CassandraChatMemoryConfig.builder().withCqlSession(cqlSession);

		builder = builder.withKeyspaceName(properties.getKeyspace())
			.withTableName(properties.getTable())
			.withAssistantColumnName(properties.getAssistantColumn())
			.withUserColumnName(properties.getUserColumn());

		if (!properties.isInitializeSchema()) {
			builder = builder.disallowSchemaChanges();
		}
		if (null != properties.getTimeToLive()) {
			builder = builder.withTimeToLive(properties.getTimeToLive());
		}

		return CassandraChatMemory.create(builder.build());
	}

}
