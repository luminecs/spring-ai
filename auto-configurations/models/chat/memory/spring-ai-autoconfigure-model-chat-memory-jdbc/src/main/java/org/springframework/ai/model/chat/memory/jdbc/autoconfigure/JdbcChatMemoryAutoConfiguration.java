package org.springframework.ai.model.chat.memory.jdbc.autoconfigure;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.memory.jdbc.JdbcChatMemory;
import org.springframework.ai.chat.memory.jdbc.JdbcChatMemoryConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnClass({ JdbcChatMemory.class, DataSource.class, JdbcTemplate.class })
@EnableConfigurationProperties(JdbcChatMemoryProperties.class)
public class JdbcChatMemoryAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(JdbcChatMemoryAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public JdbcChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
		var config = JdbcChatMemoryConfig.builder().jdbcTemplate(jdbcTemplate).build();

		return JdbcChatMemory.create(config);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "spring.ai.chat.memory.jdbc.initialize-schema", havingValue = "true",
			matchIfMissing = true)
	public DataSourceScriptDatabaseInitializer jdbcChatMemoryScriptDatabaseInitializer(DataSource dataSource) {
		logger.debug("Initializing JdbcChatMemory schema");

		return new JdbcChatMemoryDataSourceScriptDatabaseInitializer(dataSource);
	}

}
