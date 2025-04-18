package org.springframework.ai.chat.client.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.util.Map;

public final class ChatModelCallAdvisor implements CallAdvisor {

	private final ChatModel chatModel;

	public ChatModelCallAdvisor(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAroundAdvisorChain chain) {
		Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");

		ChatResponse chatResponse = chatModel.call(chatClientRequest.prompt());
		return ChatClientResponse.builder()
			.chatResponse(chatResponse)
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

}
