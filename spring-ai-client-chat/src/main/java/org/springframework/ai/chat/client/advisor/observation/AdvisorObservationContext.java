package org.springframework.ai.chat.client.advisor.observation;

import java.util.Map;

import io.micrometer.observation.Observation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class AdvisorObservationContext extends Observation.Context {

	private final String advisorName;

	private final ChatClientRequest chatClientRequest;

	private final int order;

	@Nullable
	private ChatClientResponse chatClientResponse;

	@Nullable
	private Map<String, Object> advisorResponseContext;

	@Deprecated
	public AdvisorObservationContext(String advisorName, Type advisorType, @Nullable AdvisedRequest advisorRequest,
			@Nullable Map<String, Object> advisorRequestContext, @Nullable Map<String, Object> advisorResponseContext,
			int order) {
		Assert.hasText(advisorName, "advisorName cannot be null or empty");

		this.advisorName = advisorName;
		this.chatClientRequest = advisorRequest != null ? advisorRequest.toChatClientRequest()
				: ChatClientRequest.builder().prompt(new Prompt()).build();
		if (!CollectionUtils.isEmpty(advisorRequestContext)) {
			this.chatClientRequest.context().putAll(advisorRequestContext);
		}
		if (!CollectionUtils.isEmpty(advisorResponseContext)) {
			this.chatClientResponse = ChatClientResponse.builder().context(advisorResponseContext).build();
		}
		this.order = order;
	}

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

	@Deprecated
	public Type getAdvisorType() {
		return Type.AROUND;
	}

	@Deprecated
	public AdvisedRequest getAdvisedRequest() {
		return AdvisedRequest.from(this.chatClientRequest);
	}

	@Deprecated
	public void setAdvisedRequest(@Nullable AdvisedRequest advisedRequest) {
		throw new IllegalStateException(
				"The AdvisedRequest is immutable. Build a new AdvisorObservationContext instead.");
	}

	@Deprecated
	public Map<String, Object> getAdvisorRequestContext() {
		return this.chatClientRequest.context();
	}

	@Deprecated
	public void setAdvisorRequestContext(@Nullable Map<String, Object> advisorRequestContext) {
		if (!CollectionUtils.isEmpty(advisorRequestContext)) {
			this.chatClientRequest.context().putAll(advisorRequestContext);
		}
	}

	@Nullable
	@Deprecated
	public Map<String, Object> getAdvisorResponseContext() {
		if (this.chatClientResponse != null) {
			return this.chatClientResponse.context();
		}
		return null;
	}

	@Deprecated
	public void setAdvisorResponseContext(@Nullable Map<String, Object> advisorResponseContext) {
		this.advisorResponseContext = advisorResponseContext;
	}

	@Deprecated
	public enum Type {

		BEFORE,

		AFTER,

		AROUND

	}

	public static final class Builder {

		private String advisorName;

		private ChatClientRequest chatClientRequest;

		private int order = 0;

		private AdvisedRequest advisorRequest;

		private Map<String, Object> advisorRequestContext;

		private Map<String, Object> advisorResponseContext;

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

		@Deprecated
		public Builder advisorType(Type advisorType) {
			return this;
		}

		@Deprecated
		public Builder advisedRequest(AdvisedRequest advisedRequest) {
			this.advisorRequest = advisedRequest;
			return this;
		}

		@Deprecated
		public Builder advisorRequestContext(Map<String, Object> advisorRequestContext) {
			this.advisorRequestContext = advisorRequestContext;
			return this;
		}

		@Deprecated
		public Builder advisorResponseContext(Map<String, Object> advisorResponseContext) {
			this.advisorResponseContext = advisorResponseContext;
			return this;
		}

		public AdvisorObservationContext build() {
			if (chatClientRequest != null && advisorRequest != null) {
				throw new IllegalArgumentException(
						"ChatClientRequest and AdvisedRequest cannot be set at the same time");
			}
			else if (chatClientRequest != null) {
				return new AdvisorObservationContext(this.advisorName, this.chatClientRequest, this.order);
			}
			else {
				return new AdvisorObservationContext(this.advisorName, Type.AROUND, this.advisorRequest,
						this.advisorRequestContext, this.advisorResponseContext, this.order);
			}
		}

	}

}
