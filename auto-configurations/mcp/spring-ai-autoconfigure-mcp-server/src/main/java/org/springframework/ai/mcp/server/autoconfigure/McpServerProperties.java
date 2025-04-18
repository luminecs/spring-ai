package org.springframework.ai.mcp.server.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties(McpServerProperties.CONFIG_PREFIX)
public class McpServerProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.server";

	private boolean enabled = true;

	private boolean stdio = false;

	private String name = "mcp-server";

	private String version = "1.0.0";

	private boolean resourceChangeNotification = true;

	private boolean toolChangeNotification = true;

	private boolean promptChangeNotification = true;

	private String baseUrl = "";

	private String sseEndpoint = "/sse";

	private String sseMessageEndpoint = "/mcp/message";

	private ServerType type = ServerType.SYNC;

	public enum ServerType {

		SYNC,

		ASYNC

	}

	private Map<String, String> toolResponseMimeType = new HashMap<>();

	public boolean isStdio() {
		return this.stdio;
	}

	public void setStdio(boolean stdio) {
		this.stdio = stdio;
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
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		Assert.hasText(version, "Version must not be empty");
		this.version = version;
	}

	public boolean isResourceChangeNotification() {
		return this.resourceChangeNotification;
	}

	public void setResourceChangeNotification(boolean resourceChangeNotification) {
		this.resourceChangeNotification = resourceChangeNotification;
	}

	public boolean isToolChangeNotification() {
		return this.toolChangeNotification;
	}

	public void setToolChangeNotification(boolean toolChangeNotification) {
		this.toolChangeNotification = toolChangeNotification;
	}

	public boolean isPromptChangeNotification() {
		return this.promptChangeNotification;
	}

	public void setPromptChangeNotification(boolean promptChangeNotification) {
		this.promptChangeNotification = promptChangeNotification;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		Assert.notNull(baseUrl, "Base URL must not be null");
		this.baseUrl = baseUrl;
	}

	public String getSseEndpoint() {
		return this.sseEndpoint;
	}

	public void setSseEndpoint(String sseEndpoint) {
		Assert.hasText(sseEndpoint, "SSE endpoint must not be empty");
		this.sseEndpoint = sseEndpoint;
	}

	public String getSseMessageEndpoint() {
		return this.sseMessageEndpoint;
	}

	public void setSseMessageEndpoint(String sseMessageEndpoint) {
		Assert.hasText(sseMessageEndpoint, "SSE message endpoint must not be empty");
		this.sseMessageEndpoint = sseMessageEndpoint;
	}

	public ServerType getType() {
		return this.type;
	}

	public void setType(ServerType serverType) {
		Assert.notNull(serverType, "Server type must not be null");
		this.type = serverType;
	}

	public Map<String, String> getToolResponseMimeType() {
		return this.toolResponseMimeType;
	}

}
