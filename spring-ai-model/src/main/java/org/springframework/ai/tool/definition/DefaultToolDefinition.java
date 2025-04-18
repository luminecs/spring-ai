package org.springframework.ai.tool.definition;

import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public record DefaultToolDefinition(String name, String description, String inputSchema) implements ToolDefinition {

	public DefaultToolDefinition {
		Assert.hasText(name, "name cannot be null or empty");
		Assert.hasText(description, "description cannot be null or empty");
		Assert.hasText(inputSchema, "inputSchema cannot be null or empty");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private String name;

		private String description;

		private String inputSchema;

		private Builder() {
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder inputSchema(String inputSchema) {
			this.inputSchema = inputSchema;
			return this;
		}

		public ToolDefinition build() {
			if (!StringUtils.hasText(this.description)) {
				this.description = ToolUtils.getToolDescriptionFromName(this.name);
			}
			return new DefaultToolDefinition(this.name, this.description, this.inputSchema);
		}

	}

}
