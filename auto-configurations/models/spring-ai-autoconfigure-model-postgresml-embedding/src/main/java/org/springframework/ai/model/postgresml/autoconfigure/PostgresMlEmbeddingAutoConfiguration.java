package org.springframework.ai.model.postgresml.autoconfigure;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.postgresml.PostgresMlEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnClass(PostgresMlEmbeddingModel.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.POSTGRESML,
		matchIfMissing = true)
@EnableConfigurationProperties(PostgresMlEmbeddingProperties.class)
public class PostgresMlEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PostgresMlEmbeddingModel postgresMlEmbeddingModel(JdbcTemplate jdbcTemplate,
			PostgresMlEmbeddingProperties embeddingProperties) {

		return new PostgresMlEmbeddingModel(jdbcTemplate, embeddingProperties.getOptions(),
				embeddingProperties.isCreateExtension());
	}

}
