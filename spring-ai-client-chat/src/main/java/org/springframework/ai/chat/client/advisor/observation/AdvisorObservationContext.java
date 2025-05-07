package org.springframework.ai.chat.client.advisor.observation;

import io.micrometer.observation.Observation;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class AdvisorObservationContext extends Observation.Context {

	private final String advisorName;

	private final ChatClientRequest chatClientRequest;

	private final int order;

	@Nullable
	private ChatClientResponse chatClientResponse;

	AdvisorObservationContext(String advisorName, ChatClientRequest chatClientRequest, int order) {
		Assert.hasText(advisorName, "advisorName cannot be null or empty");
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");

		this.advisorName = advisorName;
		this.chatClientRequest = chatClientRequest;
		this.order = order;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getAdvisorName() {
		return this.advisorName;
	}

	public ChatClientRequest getChatClientRequest() {
		return this.chatClientRequest;
	}

	public int getOrder() {
		return this.order;
	}

	@Nullable
	public ChatClientResponse getChatClientResponse() {
		return this.chatClientResponse;
	}

	public void setChatClientResponse(@Nullable ChatClientResponse chatClientResponse) {
		this.chatClientResponse = chatClientResponse;
	}

	public static final class Builder {

		private String advisorName;

		private ChatClientRequest chatClientRequest;

		private int order = 0;

		private Builder() {
		}

		public Builder advisorName(String advisorName) {
			this.advisorName = advisorName;
			return this;
		}

		public Builder chatClientRequest(ChatClientRequest chatClientRequest) {
			this.chatClientRequest = chatClientRequest;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public AdvisorObservationContext build() {
			return new AdvisorObservationContext(this.advisorName, this.chatClientRequest, this.order);
		}

	}

}
