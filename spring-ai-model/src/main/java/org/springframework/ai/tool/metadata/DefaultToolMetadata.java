package org.springframework.ai.tool.metadata;

public record DefaultToolMetadata(boolean returnDirect) implements ToolMetadata {

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private boolean returnDirect = false;

		private Builder() {
		}

		public Builder returnDirect(boolean returnDirect) {
			this.returnDirect = returnDirect;
			return this;
		}

		public ToolMetadata build() {
			return new DefaultToolMetadata(this.returnDirect);
		}

	}

}
