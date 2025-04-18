package org.springframework.ai.chat.client.observation;

import io.micrometer.observation.Observation;

import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ChatClientObservationContext extends Observation.Context {

	private final ChatClientRequest request;

	private final AiOperationMetadata operationMetadata = new AiOperationMetadata(AiOperationType.FRAMEWORK.value(),
			AiProvider.SPRING_AI.value());

	private final boolean stream;

	ChatClientObservationContext(ChatClientRequest chatClientRequest, boolean isStream) {
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
		this.request = chatClientRequest;
		this.stream = isStream;
	}

	public static Builder builder() {
		return new Builder();
	}

	public ChatClientRequest getRequest() {
		return this.request;
	}

	public AiOperationMetadata getOperationMetadata() {
		return this.operationMetadata;
	}

	public boolean isStream() {
		return this.stream;
	}

	@Nullable
	@Deprecated
	public String getFormat() {
		if (this.request.context().get(ChatClientAttributes.OUTPUT_FORMAT.getKey()) instanceof String format) {
			return format;
		}
		return null;
	}

	@Deprecated
	public void setFormat(@Nullable String format) {
		this.request.context().put(ChatClientAttributes.OUTPUT_FORMAT.getKey(), format);
	}

	public static final class Builder {

		private ChatClientRequest chatClientRequest;

		private String format;

		private boolean isStream = false;

		private Builder() {
		}

		public Builder request(ChatClientRequest chatClientRequest) {
			this.chatClientRequest = chatClientRequest;
			return this;
		}

		@Deprecated
		public Builder withRequest(ChatClientRequest chatClientRequest) {
			return request(chatClientRequest);
		}

		@Deprecated
		public Builder withFormat(String format) {
			this.format = format;
			return this;
		}

		public Builder stream(boolean isStream) {
			this.isStream = isStream;
			return this;
		}

		@Deprecated
		public Builder withStream(boolean isStream) {
			return stream(isStream);
		}

		public ChatClientObservationContext build() {
			if (StringUtils.hasText(format)) {
				this.chatClientRequest.context().put(ChatClientAttributes.OUTPUT_FORMAT.getKey(), format);
			}
			return new ChatClientObservationContext(this.chatClientRequest, this.isStream);
		}

	}

}
