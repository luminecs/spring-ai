package org.springframework.ai.chat.client.advisor.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@Deprecated
public record AdvisedResponse(@Nullable ChatResponse response, Map<String, Object> adviseContext) {

	public AdvisedResponse {
		Assert.notNull(adviseContext, "adviseContext cannot be null");
		Assert.noNullElements(adviseContext.keySet(), "adviseContext keys cannot be null");
		Assert.noNullElements(adviseContext.values(), "adviseContext values cannot be null");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder from(AdvisedResponse advisedResponse) {
		Assert.notNull(advisedResponse, "advisedResponse cannot be null");
		return new Builder().response(advisedResponse.response).adviseContext(advisedResponse.adviseContext);
	}

	public static AdvisedResponse from(ChatClientResponse chatClientResponse) {
		Assert.notNull(chatClientResponse, "chatClientResponse cannot be null");
		return new AdvisedResponse(chatClientResponse.chatResponse(), chatClientResponse.context());
	}

	public ChatClientResponse toChatClientResponse() {
		return new ChatClientResponse(this.response, this.adviseContext);
	}

	public AdvisedResponse updateContext(Function<Map<String, Object>, Map<String, Object>> contextTransform) {
		Assert.notNull(contextTransform, "contextTransform cannot be null");
		return new AdvisedResponse(this.response,
				Collections.unmodifiableMap(contextTransform.apply(new HashMap<>(this.adviseContext))));
	}

	public static final class Builder {

		@Nullable
		private ChatResponse response;

		private Map<String, Object> adviseContext;

		private Builder() {
		}

		public Builder response(@Nullable ChatResponse response) {
			this.response = response;
			return this;
		}

		public Builder adviseContext(Map<String, Object> adviseContext) {
			this.adviseContext = adviseContext;
			return this;
		}

		public AdvisedResponse build() {
			return new AdvisedResponse(this.response, this.adviseContext);
		}

	}

}
