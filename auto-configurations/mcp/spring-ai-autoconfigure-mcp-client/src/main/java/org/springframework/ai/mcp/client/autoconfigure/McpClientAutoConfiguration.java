package org.springframework.ai.mcp.client.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.client.autoconfigure.configurer.McpAsyncClientConfigurer;
import org.springframework.ai.mcp.client.autoconfigure.configurer.McpSyncClientConfigurer;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

@AutoConfiguration(after = { StdioTransportAutoConfiguration.class, SseHttpClientTransportAutoConfiguration.class,
		SseWebFluxTransportAutoConfiguration.class })
@ConditionalOnClass({ McpSchema.class })
@EnableConfigurationProperties(McpClientCommonProperties.class)
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class McpClientAutoConfiguration {

	private String connectedClientName(String clientName, String serverConnectionName) {
		return clientName + " - " + serverConnectionName;
	}

	@Bean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
			matchIfMissing = true)
	public List<McpSyncClient> mcpSyncClients(McpSyncClientConfigurer mcpSyncClientConfigurer,
			McpClientCommonProperties commonProperties,
			ObjectProvider<List<NamedClientMcpTransport>> transportsProvider) {

		List<McpSyncClient> mcpSyncClients = new ArrayList<>();

		List<NamedClientMcpTransport> namedTransports = transportsProvider.stream().flatMap(List::stream).toList();

		if (!CollectionUtils.isEmpty(namedTransports)) {
			for (NamedClientMcpTransport namedTransport : namedTransports) {

				McpSchema.Implementation clientInfo = new McpSchema.Implementation(
						this.connectedClientName(commonProperties.getName(), namedTransport.name()),
						commonProperties.getVersion());

				McpClient.SyncSpec syncSpec = McpClient.sync(namedTransport.transport())
					.clientInfo(clientInfo)
					.requestTimeout(commonProperties.getRequestTimeout());

				syncSpec = mcpSyncClientConfigurer.configure(namedTransport.name(), syncSpec);

				var syncClient = syncSpec.build();

				if (commonProperties.isInitialized()) {
					syncClient.initialize();
				}

				mcpSyncClients.add(syncClient);
			}
		}

		return mcpSyncClients;
	}

	@Bean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
			matchIfMissing = true)
	public CloseableMcpSyncClients makeSyncClientsClosable(List<McpSyncClient> clients) {
		return new CloseableMcpSyncClients(clients);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
			matchIfMissing = true)
	McpSyncClientConfigurer mcpSyncClientConfigurer(ObjectProvider<McpSyncClientCustomizer> customizerProvider) {
		return new McpSyncClientConfigurer(customizerProvider.orderedStream().toList());
	}

	@Bean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
	public List<McpAsyncClient> mcpAsyncClients(McpAsyncClientConfigurer mcpSyncClientConfigurer,
			McpClientCommonProperties commonProperties,
			ObjectProvider<List<NamedClientMcpTransport>> transportsProvider) {

		List<McpAsyncClient> mcpSyncClients = new ArrayList<>();

		List<NamedClientMcpTransport> namedTransports = transportsProvider.stream().flatMap(List::stream).toList();

		if (!CollectionUtils.isEmpty(namedTransports)) {
			for (NamedClientMcpTransport namedTransport : namedTransports) {

				McpSchema.Implementation clientInfo = new McpSchema.Implementation(
						this.connectedClientName(commonProperties.getName(), namedTransport.name()),
						commonProperties.getVersion());

				McpClient.AsyncSpec syncSpec = McpClient.async(namedTransport.transport())
					.clientInfo(clientInfo)
					.requestTimeout(commonProperties.getRequestTimeout());

				syncSpec = mcpSyncClientConfigurer.configure(namedTransport.name(), syncSpec);

				var syncClient = syncSpec.build();

				if (commonProperties.isInitialized()) {
					syncClient.initialize().block();
				}

				mcpSyncClients.add(syncClient);
			}
		}

		return mcpSyncClients;
	}

	@Bean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
	public CloseableMcpAsyncClients makeAsynClientsClosable(List<McpAsyncClient> clients) {
		return new CloseableMcpAsyncClients(clients);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
	McpAsyncClientConfigurer mcpAsyncClientConfigurer(ObjectProvider<McpAsyncClientCustomizer> customizerProvider) {
		return new McpAsyncClientConfigurer(customizerProvider.orderedStream().toList());
	}

	public record CloseableMcpSyncClients(List<McpSyncClient> clients) implements AutoCloseable {

		@Override
		public void close() {
			this.clients.forEach(McpSyncClient::close);
		}
	}

	public record CloseableMcpAsyncClients(List<McpAsyncClient> clients) implements AutoCloseable {
		@Override
		public void close() {
			this.clients.forEach(McpAsyncClient::close);
		}
	}

}
