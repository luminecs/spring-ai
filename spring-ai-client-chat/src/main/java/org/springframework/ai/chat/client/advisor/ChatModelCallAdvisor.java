package org.springframework.ai.chat.client.advisor;

import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;

public final class ChatModelCallAdvisor implements CallAdvisor {

	private final ChatModel chatModel;

	private ChatModelCallAdvisor(ChatModel chatModel) {
		Assert.notNull(chatModel, "chatModel cannot be null");
		this.chatModel = chatModel;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");

		ChatClientRequest formattedChatClientRequest = augmentWithFormatInstructions(chatClientRequest);

		ChatResponse chatResponse = chatModel.call(formattedChatClientRequest.prompt());
		return ChatClientResponse.builder()
			.chatResponse(chatResponse)
			.context(Map.copyOf(formattedChatClientRequest.context()))
			.build();
	}

	private static ChatClientRequest augmentWithFormatInstructions(ChatClientRequest chatClientRequest) {
		String outputFormat = (String) chatClientRequest.context().get(ChatClientAttributes.OUTPUT_FORMAT.getKey());

		if (!StringUtils.hasText(outputFormat)) {
			return chatClientRequest;
		}

		Prompt augmentedPrompt = chatClientRequest.prompt()
			.augmentUserMessage(userMessage -> userMessage.mutate()
				.text(userMessage.getText() + System.lineSeparator() + outputFormat)
				.build());

		return ChatClientRequest.builder()
			.prompt(augmentedPrompt)
			.context(Map.copyOf(chatClientRequest.context()))
			.build();
	}

	@Override
	public String getName() {
		return "call";
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ChatModel chatModel;

		private Builder() {
		}

		public Builder chatModel(ChatModel chatModel) {
			this.chatModel = chatModel;
			return this;
		}

		public ChatModelCallAdvisor build() {
			return new ChatModelCallAdvisor(this.chatModel);
		}

	}

}
