package org.springframework.ai.chat.client.advisor.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public record AdvisedRequest(
// @formatter:off
		ChatModel chatModel,
		String userText,
		@Nullable
		String systemText,
		@Nullable
		ChatOptions chatOptions,
		List<Media> media,
		List<String> toolNames,
		List<ToolCallback> toolCallbacks,
		List<Message> messages,
		Map<String, Object> userParams,
		Map<String, Object> systemParams,
		List<Advisor> advisors,
		@Deprecated
		Map<String, Object> advisorParams,
		Map<String, Object> adviseContext,
		Map<String, Object> toolContext
// @formatter:on
) {

	public AdvisedRequest {
		Assert.notNull(chatModel, "chatModel cannot be null");
		Assert.isTrue(StringUtils.hasText(userText) || !CollectionUtils.isEmpty(messages),
				"userText cannot be null or empty unless messages are provided and contain Tool Response message.");
		Assert.notNull(media, "media cannot be null");
		Assert.noNullElements(media, "media cannot contain null elements");
		Assert.notNull(toolNames, "toolNames cannot be null");
		Assert.noNullElements(toolNames, "toolNames cannot contain null elements");
		Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");
		Assert.notNull(userParams, "userParams cannot be null");
		Assert.noNullElements(userParams.keySet(), "userParams keys cannot contain null elements");
		Assert.noNullElements(userParams.values(), "userParams values cannot contain null elements");
		Assert.notNull(systemParams, "systemParams cannot be null");
		Assert.noNullElements(systemParams.keySet(), "systemParams keys cannot contain null elements");
		Assert.noNullElements(systemParams.values(), "systemParams values cannot contain null elements");
		Assert.notNull(advisors, "advisors cannot be null");
		Assert.noNullElements(advisors, "advisors cannot contain null elements");
		Assert.notNull(advisorParams, "advisorParams cannot be null");
		Assert.noNullElements(advisorParams.keySet(), "advisorParams keys cannot contain null elements");
		Assert.noNullElements(advisorParams.values(), "advisorParams values cannot contain null elements");
		Assert.notNull(adviseContext, "adviseContext cannot be null");
		Assert.noNullElements(adviseContext.keySet(), "adviseContext keys cannot contain null elements");
		Assert.noNullElements(adviseContext.values(), "adviseContext values cannot contain null elements");
		Assert.notNull(toolContext, "toolContext cannot be null");
		Assert.noNullElements(toolContext.keySet(), "toolContext keys cannot contain null elements");
		Assert.noNullElements(toolContext.values(), "toolContext values cannot contain null elements");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder from(AdvisedRequest from) {
		Assert.notNull(from, "AdvisedRequest cannot be null");

		Builder builder = new Builder();
		builder.chatModel = from.chatModel;
		builder.userText = from.userText;
		builder.systemText = from.systemText;
		builder.chatOptions = from.chatOptions;
		builder.media = from.media;
		builder.toolNames = from.toolNames;
		builder.toolCallbacks = from.toolCallbacks;
		builder.messages = from.messages;
		builder.userParams = from.userParams;
		builder.systemParams = from.systemParams;
		builder.advisors = from.advisors;
		builder.advisorParams = from.advisorParams;
		builder.adviseContext = from.adviseContext;
		builder.toolContext = from.toolContext;
		return builder;
	}

	@SuppressWarnings("unchecked")
	public static AdvisedRequest from(ChatClientRequest from) {
		Assert.notNull(from, "ChatClientRequest cannot be null");

		List<Message> messages = new LinkedList<>(from.prompt().getInstructions());

		Builder builder = new Builder();
		if (from.context().get(ChatClientAttributes.CHAT_MODEL.getKey()) instanceof ChatModel chatModel) {
			builder.chatModel = chatModel;
		}

		if (!messages.isEmpty() && messages.get(messages.size() - 1) instanceof UserMessage userMessage) {
			builder.userText = userMessage.getText();
			builder.media = userMessage.getMedia();
			messages.remove(messages.size() - 1);
		}
		if (from.context().get(ChatClientAttributes.USER_PARAMS.getKey()) instanceof Map<?, ?> contextUserParams) {
			builder.userParams = (Map<String, Object>) contextUserParams;
		}

		if (!messages.isEmpty() && messages.get(messages.size() - 1) instanceof SystemMessage systemMessage) {
			builder.systemText = systemMessage.getText();
			messages.remove(messages.size() - 1);
		}
		if (from.context().get(ChatClientAttributes.SYSTEM_PARAMS.getKey()) instanceof Map<?, ?> contextSystemParams) {
			builder.systemParams = (Map<String, Object>) contextSystemParams;
		}

		builder.messages = messages;

		builder.chatOptions = Objects.requireNonNullElse(from.prompt().getOptions(), ChatOptions.builder().build());
		if (from.prompt().getOptions() instanceof ToolCallingChatOptions options) {
			builder.toolNames = options.getToolNames().stream().toList();
			builder.toolCallbacks = options.getToolCallbacks();
			builder.toolContext = options.getToolContext();
		}

		if (from.context().get(ChatClientAttributes.ADVISORS.getKey()) instanceof List<?> advisors) {
			builder.advisors = (List<Advisor>) advisors;
		}
		builder.advisorParams = Map.of();
		builder.adviseContext = from.context();

		return builder.build();
	}

	public AdvisedRequest updateContext(Function<Map<String, Object>, Map<String, Object>> contextTransform) {
		Assert.notNull(contextTransform, "contextTransform cannot be null");
		return from(this)
			.adviseContext(Collections.unmodifiableMap(contextTransform.apply(new HashMap<>(this.adviseContext))))
			.build();
	}

	public ChatClientRequest toChatClientRequest() {
		return ChatClientRequest.builder()
			.prompt(toPrompt())
			.context(this.adviseContext)
			.context(ChatClientAttributes.ADVISORS.getKey(), this.advisors)
			.context(ChatClientAttributes.CHAT_MODEL.getKey(), this.chatModel)
			.context(ChatClientAttributes.USER_PARAMS.getKey(), this.userParams)
			.context(ChatClientAttributes.SYSTEM_PARAMS.getKey(), this.systemParams)
			.build();
	}

	public Prompt toPrompt() {
		var messages = new ArrayList<>(this.messages());

		String processedSystemText = this.systemText();
		if (StringUtils.hasText(processedSystemText)) {
			if (!CollectionUtils.isEmpty(this.systemParams())) {
				processedSystemText = new PromptTemplate(processedSystemText, this.systemParams()).render();
			}
			messages.add(new SystemMessage(processedSystemText));
		}

		if (StringUtils.hasText(this.userText())) {
			Map<String, Object> userParams = new HashMap<>(this.userParams());
			String processedUserText = this.userText();
			if (!CollectionUtils.isEmpty(userParams)) {
				processedUserText = new PromptTemplate(processedUserText, userParams).render();
			}
			messages.add(new UserMessage(processedUserText, this.media()));
		}

		if (this.chatOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
			if (!this.toolNames().isEmpty()) {
				toolCallingChatOptions.setToolNames(new HashSet<>(this.toolNames()));
			}
			if (!this.toolCallbacks().isEmpty()) {
				toolCallingChatOptions.setToolCallbacks(this.toolCallbacks());
			}
			if (!CollectionUtils.isEmpty(this.toolContext())) {
				toolCallingChatOptions.setToolContext(this.toolContext());
			}
		}

		return new Prompt(messages, this.chatOptions());
	}

	public static final class Builder {

		private ChatModel chatModel;

		private String userText;

		private String systemText;

		private ChatOptions chatOptions;

		private List<Media> media = List.of();

		private List<String> toolNames = List.of();

		private List<ToolCallback> toolCallbacks = List.of();

		private List<Message> messages = List.of();

		private Map<String, Object> userParams = Map.of();

		private Map<String, Object> systemParams = Map.of();

		private List<Advisor> advisors = List.of();

		private Map<String, Object> advisorParams = Map.of();

		private Map<String, Object> adviseContext = Map.of();

		public Map<String, Object> toolContext = Map.of();

		private Builder() {
		}

		public Builder chatModel(ChatModel chatModel) {
			this.chatModel = chatModel;
			return this;
		}

		public Builder userText(String userText) {
			this.userText = userText;
			return this;
		}

		public Builder systemText(String systemText) {
			this.systemText = systemText;
			return this;
		}

		public Builder chatOptions(ChatOptions chatOptions) {
			this.chatOptions = chatOptions;
			return this;
		}

		public Builder media(List<Media> media) {
			this.media = media;
			return this;
		}

		public Builder toolNames(List<String> toolNames) {
			this.toolNames = toolNames;
			return this;
		}

		public Builder functionCallbacks(List<ToolCallback> toolCallbacks) {
			this.toolCallbacks = toolCallbacks;
			return this;
		}

		public Builder messages(List<Message> messages) {
			this.messages = messages;
			return this;
		}

		public Builder userParams(Map<String, Object> userParams) {
			this.userParams = userParams;
			return this;
		}

		public Builder systemParams(Map<String, Object> systemParams) {
			this.systemParams = systemParams;
			return this;
		}

		public Builder advisors(List<Advisor> advisors) {
			this.advisors = advisors;
			return this;
		}

		@Deprecated
		public Builder advisorParams(Map<String, Object> advisorParams) {
			this.advisorParams = advisorParams;
			return this;
		}

		public Builder adviseContext(Map<String, Object> adviseContext) {
			this.adviseContext = adviseContext;
			return this;
		}

		public Builder toolContext(Map<String, Object> toolContext) {
			this.toolContext = toolContext;
			return this;
		}

		public AdvisedRequest build() {
			return new AdvisedRequest(this.chatModel, this.userText, this.systemText, this.chatOptions, this.media,
					this.toolNames, this.toolCallbacks, this.messages, this.userParams, this.systemParams,
					this.advisors, this.advisorParams, this.adviseContext, this.toolContext);
		}

	}

}
