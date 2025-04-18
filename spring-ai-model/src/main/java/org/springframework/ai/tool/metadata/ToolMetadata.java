package org.springframework.ai.tool.metadata;

import java.lang.reflect.Method;

import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.util.Assert;

public interface ToolMetadata {

	default boolean returnDirect() {
		return false;
	}

	static DefaultToolMetadata.Builder builder() {
		return DefaultToolMetadata.builder();
	}

	static ToolMetadata from(Method method) {
		Assert.notNull(method, "method cannot be null");
		return DefaultToolMetadata.builder().returnDirect(ToolUtils.getToolReturnDirect(method)).build();
	}

}
