package org.springframework.ai.mcp.server.autoconfigure;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class McpServerStdioDisabledCondition extends AllNestedConditions {

	public McpServerStdioDisabledCondition() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}

	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	static class McpServerEnabledCondition {

	}

	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "stdio", havingValue = "false",
			matchIfMissing = true)
	static class StdioDisabledCondition {

	}

}
