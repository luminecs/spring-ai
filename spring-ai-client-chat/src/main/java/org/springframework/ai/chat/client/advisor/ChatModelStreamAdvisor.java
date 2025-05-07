package org.springframework.ai.chat.client.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

public final class ChatModelStreamAdvisor implements StreamAdvisor {

	private final ChatModel chatModel;

	private ChatModelStreamAdvisor(ChatModel chatModel) {
		Assert.notNull(chatModel, "chatModel cannot be null");
		this.chatModel = chatModel;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");

		return chatModel.stream(chatClientRequest.prompt())
			.map(chatResponse -> ChatClientResponse.builder()
				.chatResponse(chatResponse)
				.context(Map.copyOf(chatClientRequest.context()))
				.build())
			.publishOn(Schedulers.boundedElastic());
	}

	@Override
	public String getName() {
		return "stream";
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

		public ChatModelStreamAdvisor build() {
			return new ChatModelStreamAdvisor(this.chatModel);
		}

	}

}
