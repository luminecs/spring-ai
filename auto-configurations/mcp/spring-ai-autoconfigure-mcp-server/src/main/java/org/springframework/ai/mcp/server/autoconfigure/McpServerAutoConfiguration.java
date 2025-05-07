package org.springframework.ai.mcp.server.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.AsyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import reactor.core.publisher.Mono;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.log.LogAccessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;

@AutoConfiguration(after = { McpWebMvcServerAutoConfiguration.class, McpWebFluxServerAutoConfiguration.class })
@ConditionalOnClass({ McpSchema.class, McpSyncServer.class })
@EnableConfigurationProperties(McpServerProperties.class)
@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class McpServerAutoConfiguration {

	private static final LogAccessor logger = new LogAccessor(McpServerAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	public McpServerTransportProvider stdioServerTransport() {
		return new StdioServerTransportProvider();
	}

	@Bean
	@ConditionalOnMissingBean
	public McpSchema.ServerCapabilities.Builder capabilitiesBuilder() {
		return McpSchema.ServerCapabilities.builder();
	}

	@Bean
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
			matchIfMissing = true)
	public List<McpServerFeatures.SyncToolSpecification> syncTools(ObjectProvider<List<ToolCallback>> toolCalls,
			List<ToolCallback> toolCallbacksList, McpServerProperties serverProperties) {

		List<ToolCallback> tools = new ArrayList<>(toolCalls.stream().flatMap(List::stream).toList());

		if (!CollectionUtils.isEmpty(toolCallbacksList)) {
			tools.addAll(toolCallbacksList);
		}

		return this.toSyncToolSpecifications(tools, serverProperties);
	}

	private List<McpServerFeatures.SyncToolSpecification> toSyncToolSpecifications(List<ToolCallback> tools,
			McpServerProperties serverProperties) {

		return tools.stream()
			.collect(Collectors.toMap(tool -> tool.getToolDefinition().name(),

					tool -> tool, (existing, replacement) -> existing))

			.values()
			.stream()
			.map(tool -> {
				String toolName = tool.getToolDefinition().name();
				MimeType mimeType = (serverProperties.getToolResponseMimeType().containsKey(toolName))
						? MimeType.valueOf(serverProperties.getToolResponseMimeType().get(toolName)) : null;
				return McpToolUtils.toSyncToolSpecification(tool, mimeType);
			})
			.toList();
	}

	@Bean
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
			matchIfMissing = true)
	public McpSyncServer mcpSyncServer(McpServerTransportProvider transportProvider,
			McpSchema.ServerCapabilities.Builder capabilitiesBuilder, McpServerProperties serverProperties,
			ObjectProvider<List<SyncToolSpecification>> tools,
			ObjectProvider<List<SyncResourceSpecification>> resources,
			ObjectProvider<List<SyncPromptSpecification>> prompts,
			ObjectProvider<List<SyncCompletionSpecification>> completions,
			ObjectProvider<BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>> rootsChangeConsumers,
			List<ToolCallbackProvider> toolCallbackProvider) {

		McpSchema.Implementation serverInfo = new Implementation(serverProperties.getName(),
				serverProperties.getVersion());

		SyncSpecification serverBuilder = McpServer.sync(transportProvider).serverInfo(serverInfo);

		List<SyncToolSpecification> toolSpecifications = new ArrayList<>(tools.stream().flatMap(List::stream).toList());

		List<ToolCallback> providerToolCallbacks = toolCallbackProvider.stream()
			.map(pr -> List.of(pr.getToolCallbacks()))
			.flatMap(List::stream)
			.filter(fc -> fc instanceof ToolCallback)
			.map(fc -> (ToolCallback) fc)
			.toList();

		toolSpecifications.addAll(this.toSyncToolSpecifications(providerToolCallbacks, serverProperties));

		if (!CollectionUtils.isEmpty(toolSpecifications)) {
			serverBuilder.tools(toolSpecifications);
			capabilitiesBuilder.tools(serverProperties.isToolChangeNotification());
			logger.info("Registered tools: " + toolSpecifications.size() + ", notification: "
					+ serverProperties.isToolChangeNotification());
		}

		List<SyncResourceSpecification> resourceSpecifications = resources.stream().flatMap(List::stream).toList();
		if (!CollectionUtils.isEmpty(resourceSpecifications)) {
			serverBuilder.resources(resourceSpecifications);
			capabilitiesBuilder.resources(false, serverProperties.isResourceChangeNotification());
			logger.info("Registered resources: " + resourceSpecifications.size() + ", notification: "
					+ serverProperties.isResourceChangeNotification());
		}

		List<SyncPromptSpecification> promptSpecifications = prompts.stream().flatMap(List::stream).toList();
		if (!CollectionUtils.isEmpty(promptSpecifications)) {
			serverBuilder.prompts(promptSpecifications);
			capabilitiesBuilder.prompts(serverProperties.isPromptChangeNotification());
			logger.info("Registered prompts: " + promptSpecifications.size() + ", notification: "
					+ serverProperties.isPromptChangeNotification());
		}

		List<SyncCompletionSpecification> completionSpecifications = completions.stream()
			.flatMap(List::stream)
			.toList();
		if (!CollectionUtils.isEmpty(completionSpecifications)) {
			serverBuilder.completions(completionSpecifications);
			capabilitiesBuilder.completions();
			logger.info("Registered completions: " + completionSpecifications.size());
		}

		rootsChangeConsumers.ifAvailable(consumer -> {
			serverBuilder.rootsChangeHandler((exchange, roots) -> consumer.accept(exchange, roots));
			logger.info("Registered roots change consumer");
		});

		serverBuilder.capabilities(capabilitiesBuilder.build());

		serverBuilder.instructions(serverProperties.getInstructions());

		return serverBuilder.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
	public List<McpServerFeatures.AsyncToolSpecification> asyncTools(ObjectProvider<List<ToolCallback>> toolCalls,
			List<ToolCallback> toolCallbackList, McpServerProperties serverProperties) {

		List<ToolCallback> tools = new ArrayList<>(toolCalls.stream().flatMap(List::stream).toList());
		if (!CollectionUtils.isEmpty(toolCallbackList)) {
			tools.addAll(toolCallbackList);
		}

		return this.toAsyncToolSpecification(tools, serverProperties);
	}

	private List<McpServerFeatures.AsyncToolSpecification> toAsyncToolSpecification(List<ToolCallback> tools,
			McpServerProperties serverProperties) {

		return tools.stream()
			.collect(Collectors.toMap(tool -> tool.getToolDefinition().name(),

					tool -> tool, (existing, replacement) -> existing))

			.values()
			.stream()
			.map(tool -> {
				String toolName = tool.getToolDefinition().name();
				MimeType mimeType = (serverProperties.getToolResponseMimeType().containsKey(toolName))
						? MimeType.valueOf(serverProperties.getToolResponseMimeType().get(toolName)) : null;
				return McpToolUtils.toAsyncToolSpecification(tool, mimeType);
			})
			.toList();
	}

	@Bean
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
	public McpAsyncServer mcpAsyncServer(McpServerTransportProvider transportProvider,
			McpSchema.ServerCapabilities.Builder capabilitiesBuilder, McpServerProperties serverProperties,
			ObjectProvider<List<AsyncToolSpecification>> tools,
			ObjectProvider<List<AsyncResourceSpecification>> resources,
			ObjectProvider<List<AsyncPromptSpecification>> prompts,
			ObjectProvider<List<AsyncCompletionSpecification>> completions,
			ObjectProvider<BiConsumer<McpAsyncServerExchange, List<McpSchema.Root>>> rootsChangeConsumer,
			List<ToolCallbackProvider> toolCallbackProvider) {

		McpSchema.Implementation serverInfo = new Implementation(serverProperties.getName(),
				serverProperties.getVersion());

		AsyncSpecification serverBuilder = McpServer.async(transportProvider).serverInfo(serverInfo);

		List<AsyncToolSpecification> toolSpecifications = new ArrayList<>(
				tools.stream().flatMap(List::stream).toList());
		List<ToolCallback> providerToolCallbacks = toolCallbackProvider.stream()
			.map(pr -> List.of(pr.getToolCallbacks()))
			.flatMap(List::stream)
			.filter(fc -> fc instanceof ToolCallback)
			.map(fc -> (ToolCallback) fc)
			.toList();

		toolSpecifications.addAll(this.toAsyncToolSpecification(providerToolCallbacks, serverProperties));

		if (!CollectionUtils.isEmpty(toolSpecifications)) {
			serverBuilder.tools(toolSpecifications);
			capabilitiesBuilder.tools(serverProperties.isToolChangeNotification());
			logger.info("Registered tools: " + toolSpecifications.size() + ", notification: "
					+ serverProperties.isToolChangeNotification());
		}

		List<AsyncResourceSpecification> resourceSpecifications = resources.stream().flatMap(List::stream).toList();
		if (!CollectionUtils.isEmpty(resourceSpecifications)) {
			serverBuilder.resources(resourceSpecifications);
			capabilitiesBuilder.resources(false, serverProperties.isResourceChangeNotification());
			logger.info("Registered resources: " + resourceSpecifications.size() + ", notification: "
					+ serverProperties.isResourceChangeNotification());
		}

		List<AsyncPromptSpecification> promptSpecifications = prompts.stream().flatMap(List::stream).toList();
		if (!CollectionUtils.isEmpty(promptSpecifications)) {
			serverBuilder.prompts(promptSpecifications);
			capabilitiesBuilder.prompts(serverProperties.isPromptChangeNotification());
			logger.info("Registered prompts: " + promptSpecifications.size() + ", notification: "
					+ serverProperties.isPromptChangeNotification());
		}

		List<AsyncCompletionSpecification> completionSpecifications = completions.stream()
			.flatMap(List::stream)
			.toList();
		if (!CollectionUtils.isEmpty(completionSpecifications)) {
			serverBuilder.completions(completionSpecifications);
			capabilitiesBuilder.completions();
			logger.info("Registered completions: " + completionSpecifications.size());
		}

		rootsChangeConsumer.ifAvailable(consumer -> {
			BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>> asyncConsumer = (exchange, roots) -> {
				consumer.accept(exchange, roots);
				return Mono.empty();
			};
			serverBuilder.rootsChangeHandler(asyncConsumer);
			logger.info("Registered roots change consumer");
		});

		serverBuilder.capabilities(capabilitiesBuilder.build());

		serverBuilder.instructions(serverProperties.getInstructions());

		return serverBuilder.build();
	}

}