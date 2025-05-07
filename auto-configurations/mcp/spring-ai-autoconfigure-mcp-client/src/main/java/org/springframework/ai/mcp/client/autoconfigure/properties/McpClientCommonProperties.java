package org.springframework.ai.mcp.client.autoconfigure.properties;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(McpClientCommonProperties.CONFIG_PREFIX)
public class McpClientCommonProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.client";

	private boolean enabled = true;

	private String name = "spring-ai-mcp-client";

	private String version = "1.0.0";

	private boolean initialized = true;

	private Duration requestTimeout = Duration.ofSeconds(20);

	private ClientType type = ClientType.SYNC;

	public enum ClientType {

		SYNC,

		ASYNC

	}

	private boolean rootChangeNotification = true;

	private Toolcallback toolcallback = new Toolcallback(false);

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	public record Toolcallback(

			@JsonProperty("enabled") boolean enabled) {
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public Duration getRequestTimeout() {
		return this.requestTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public ClientType getType() {
		return this.type;
	}

	public void setType(ClientType type) {
		this.type = type;
	}

	public boolean isRootChangeNotification() {
		return this.rootChangeNotification;
	}

	public void setRootChangeNotification(boolean rootChangeNotification) {
		this.rootChangeNotification = rootChangeNotification;
	}

	public Toolcallback getToolcallback() {
		return toolcallback;
	}

	public void setToolcallback(Toolcallback toolcallback) {
		this.toolcallback = toolcallback;
	}

}
