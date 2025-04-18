package org.springframework.ai.chat.memory.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

public final class JdbcChatMemoryConfig {

	private final JdbcTemplate jdbcTemplate;

	private JdbcChatMemoryConfig(Builder builder) {
		this.jdbcTemplate = builder.jdbcTemplate;
	}

	public static Builder builder() {
		return new Builder();
	}

	JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public static final class Builder {

		private JdbcTemplate jdbcTemplate;

		private Builder() {
		}

		public Builder jdbcTemplate(JdbcTemplate jdbcTemplate) {
			Assert.notNull(jdbcTemplate, "jdbc template must not be null");

			this.jdbcTemplate = jdbcTemplate;
			return this;
		}

		public JdbcChatMemoryConfig build() {
			Assert.notNull(this.jdbcTemplate, "jdbc template must not be null");

			return new JdbcChatMemoryConfig(this);
		}

	}

}
