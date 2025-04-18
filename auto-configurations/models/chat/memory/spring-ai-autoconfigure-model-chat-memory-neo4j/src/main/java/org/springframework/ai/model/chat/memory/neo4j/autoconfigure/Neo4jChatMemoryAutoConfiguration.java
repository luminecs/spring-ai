package org.springframework.ai.model.chat.memory.neo4j.autoconfigure;

import org.neo4j.driver.Driver;

import org.springframework.ai.chat.memory.neo4j.Neo4jChatMemory;
import org.springframework.ai.chat.memory.neo4j.Neo4jChatMemoryConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = Neo4jAutoConfiguration.class)
@ConditionalOnClass({ Neo4jChatMemory.class, Driver.class })
@EnableConfigurationProperties(Neo4jChatMemoryProperties.class)
public class Neo4jChatMemoryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Neo4jChatMemory chatMemory(Neo4jChatMemoryProperties properties, Driver driver) {

		var builder = Neo4jChatMemoryConfig.builder()
			.withMediaLabel(properties.getMediaLabel())
			.withMessageLabel(properties.getMessageLabel())
			.withMetadataLabel(properties.getMetadataLabel())
			.withSessionLabel(properties.getSessionLabel())
			.withToolCallLabel(properties.getToolCallLabel())
			.withToolResponseLabel(properties.getToolResponseLabel())
			.withDriver(driver);

		return Neo4jChatMemory.create(builder.build());
	}

}
