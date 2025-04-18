package org.springframework.ai.tool.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;

public class DefaultToolExecutionExceptionProcessor implements ToolExecutionExceptionProcessor {

	private final static Logger logger = LoggerFactory.getLogger(DefaultToolExecutionExceptionProcessor.class);

	private static final boolean DEFAULT_ALWAYS_THROW = false;

	private final boolean alwaysThrow;

	public DefaultToolExecutionExceptionProcessor(boolean alwaysThrow) {
		this.alwaysThrow = alwaysThrow;
	}

	@Override
	public String process(ToolExecutionException exception) {
		Assert.notNull(exception, "exception cannot be null");
		if (this.alwaysThrow) {
			throw exception;
		}
		logger.debug("Exception thrown by tool: {}. Message: {}", exception.getToolDefinition().name(),
				exception.getMessage());
		return exception.getMessage();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private boolean alwaysThrow = DEFAULT_ALWAYS_THROW;

		public Builder alwaysThrow(boolean alwaysThrow) {
			this.alwaysThrow = alwaysThrow;
			return this;
		}

		public DefaultToolExecutionExceptionProcessor build() {
			return new DefaultToolExecutionExceptionProcessor(this.alwaysThrow);
		}

	}

}
