package org.springframework.ai.chat.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.model.ModelResponse;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ChatResponse implements ModelResponse<Generation> {

	private final ChatResponseMetadata chatResponseMetadata;

	private final List<Generation> generations;

	public ChatResponse(List<Generation> generations) {
		this(generations, new ChatResponseMetadata());
	}

	public ChatResponse(List<Generation> generations, ChatResponseMetadata chatResponseMetadata) {
		this.chatResponseMetadata = chatResponseMetadata;
		this.generations = List.copyOf(generations);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public List<Generation> getResults() {
		return this.generations;
	}

	public Generation getResult() {
		if (CollectionUtils.isEmpty(this.generations)) {
			return null;
		}
		return this.generations.get(0);
	}

	@Override
	public ChatResponseMetadata getMetadata() {
		return this.chatResponseMetadata;
	}

	public boolean hasToolCalls() {
		if (CollectionUtils.isEmpty(this.generations)) {
			return false;
		}
		return this.generations.stream().anyMatch(generation -> generation.getOutput().hasToolCalls());
	}

	public boolean hasFinishReasons(Set<String> finishReasons) {
		Assert.notNull(finishReasons, "finishReasons cannot be null");
		if (CollectionUtils.isEmpty(this.generations)) {
			return false;
		}
		return this.generations.stream().anyMatch(generation -> {
			var finishReason = (generation.getMetadata().getFinishReason() != null)
					? generation.getMetadata().getFinishReason() : "";
			return finishReasons.stream().map(String::toLowerCase).toList().contains(finishReason.toLowerCase());
		});
	}

	@Override
	public String toString() {
		return "ChatResponse [metadata=" + this.chatResponseMetadata + ", generations=" + this.generations + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ChatResponse that)) {
			return false;
		}
		return Objects.equals(this.chatResponseMetadata, that.chatResponseMetadata)
				&& Objects.equals(this.generations, that.generations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.chatResponseMetadata, this.generations);
	}

	public static final class Builder {

		private List<Generation> generations;

		private ChatResponseMetadata.Builder chatResponseMetadataBuilder;

		private Builder() {
			this.chatResponseMetadataBuilder = ChatResponseMetadata.builder();
		}

		public Builder from(ChatResponse other) {
			this.generations = other.generations;
			return this.metadata(other.chatResponseMetadata);
		}

		public Builder metadata(String key, Object value) {
			this.chatResponseMetadataBuilder.keyValue(key, value);
			return this;
		}

		public Builder metadata(ChatResponseMetadata other) {
			this.chatResponseMetadataBuilder.model(other.getModel());
			this.chatResponseMetadataBuilder.id(other.getId());
			this.chatResponseMetadataBuilder.rateLimit(other.getRateLimit());
			this.chatResponseMetadataBuilder.usage(other.getUsage());
			this.chatResponseMetadataBuilder.promptMetadata(other.getPromptMetadata());
			Set<Map.Entry<String, Object>> entries = other.entrySet();
			for (Map.Entry<String, Object> entry : entries) {
				this.chatResponseMetadataBuilder.keyValue(entry.getKey(), entry.getValue());
			}
			return this;
		}

		public Builder generations(List<Generation> generations) {
			this.generations = generations;
			return this;

		}

		public ChatResponse build() {
			return new ChatResponse(this.generations, this.chatResponseMetadataBuilder.build());
		}

	}

}
