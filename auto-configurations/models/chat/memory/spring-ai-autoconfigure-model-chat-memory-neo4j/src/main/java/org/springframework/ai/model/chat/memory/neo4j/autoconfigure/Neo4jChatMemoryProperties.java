package org.springframework.ai.model.chat.memory.neo4j.autoconfigure;

import org.springframework.ai.chat.memory.neo4j.Neo4jChatMemoryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(Neo4jChatMemoryProperties.CONFIG_PREFIX)
public class Neo4jChatMemoryProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.memory.neo4j";

	private String sessionLabel = Neo4jChatMemoryConfig.DEFAULT_SESSION_LABEL;

	private String toolCallLabel = Neo4jChatMemoryConfig.DEFAULT_TOOL_CALL_LABEL;

	private String metadataLabel = Neo4jChatMemoryConfig.DEFAULT_METADATA_LABEL;

	private String messageLabel = Neo4jChatMemoryConfig.DEFAULT_MESSAGE_LABEL;

	private String toolResponseLabel = Neo4jChatMemoryConfig.DEFAULT_TOOL_RESPONSE_LABEL;

	private String mediaLabel = Neo4jChatMemoryConfig.DEFAULT_MEDIA_LABEL;

	public String getSessionLabel() {
		return this.sessionLabel;
	}

	public void setSessionLabel(String sessionLabel) {
		this.sessionLabel = sessionLabel;
	}

	public String getToolCallLabel() {
		return this.toolCallLabel;
	}

	public String getMetadataLabel() {
		return this.metadataLabel;
	}

	public String getMessageLabel() {
		return this.messageLabel;
	}

	public String getToolResponseLabel() {
		return this.toolResponseLabel;
	}

	public String getMediaLabel() {
		return this.mediaLabel;
	}

	public void setToolCallLabel(String toolCallLabel) {
		this.toolCallLabel = toolCallLabel;
	}

	public void setMetadataLabel(String metadataLabel) {
		this.metadataLabel = metadataLabel;
	}

	public void setMessageLabel(String messageLabel) {
		this.messageLabel = messageLabel;
	}

	public void setToolResponseLabel(String toolResponseLabel) {
		this.toolResponseLabel = toolResponseLabel;
	}

	public void setMediaLabel(String mediaLabel) {
		this.mediaLabel = mediaLabel;
	}

}
