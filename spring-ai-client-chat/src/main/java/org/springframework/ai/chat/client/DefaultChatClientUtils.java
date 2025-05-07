package org.springframework.ai.chat.client;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class DefaultChatClientUtils {

	static ChatClientRequest toChatClientRequest(DefaultChatClient.DefaultChatClientRequestSpec inputRequest) {
		Assert.notNull(inputRequest, "inputRequest cannot be null");

		List<Message> processedMessages = new ArrayList<>();

		String processedSystemText = inputRequest.getSystemText();
		if (StringUtils.hasText(processedSystemText)) {
			if (!CollectionUtils.isEmpty(inputRequest.getSystemParams())) {
				processedSystemText = PromptTemplate.builder()
					.template(processedSystemText)
					.variables(inputRequest.getSystemParams())
					.renderer(inputRequest.getTemplateRenderer())
					.build()
					.render();
			}
			processedMessages.add(new SystemMessage(processedSystemText));
		}

		if (!CollectionUtils.isEmpty(inputRequest.getMessages())) {
			processedMessages.addAll(inputRequest.getMessages());
		}

		String processedUserText = inputRequest.getUserText();
		if (StringUtils.hasText(processedUserText)) {
			if (!CollectionUtils.isEmpty(inputRequest.getUserParams())) {
				processedUserText = PromptTemplate.builder()
					.template(processedUserText)
					.variables(inputRequest.getUserParams())
					.renderer(inputRequest.getTemplateRenderer())
					.build()
					.render();
			}
			processedMessages.add(UserMessage.builder().text(processedUserText).media(inputRequest.getMedia()).build());
		}

		ChatOptions processedChatOptions = inputRequest.getChatOptions();
		if (processedChatOptions instanceof ToolCallingChatOptions toolCallingChatOptions) {
			if (!inputRequest.getToolNames().isEmpty()) {
				Set<String> toolNames = ToolCallingChatOptions
					.mergeToolNames(new HashSet<>(inputRequest.getToolNames()), toolCallingChatOptions.getToolNames());
				toolCallingChatOptions.setToolNames(toolNames);
			}
			if (!inputRequest.getToolCallbacks().isEmpty()) {
				List<ToolCallback> toolCallbacks = ToolCallingChatOptions
					.mergeToolCallbacks(inputRequest.getToolCallbacks(), toolCallingChatOptions.getToolCallbacks());
				ToolCallingChatOptions.validateToolCallbacks(toolCallbacks);
				toolCallingChatOptions.setToolCallbacks(toolCallbacks);
			}
			if (!CollectionUtils.isEmpty(inputRequest.getToolContext())) {
				Map<String, Object> toolContext = ToolCallingChatOptions.mergeToolContext(inputRequest.getToolContext(),
						toolCallingChatOptions.getToolContext());
				toolCallingChatOptions.setToolContext(toolContext);
			}
		}

		return ChatClientRequest.builder()
			.prompt(Prompt.builder().messages(processedMessages).chatOptions(processedChatOptions).build())
			.context(new ConcurrentHashMap<>(inputRequest.getAdvisorParams()))
			.build();
	}

}
