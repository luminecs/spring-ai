package org.springframework.ai.chat.client.advisor;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;

import java.util.function.Predicate;

public final class AdvisorUtils {

	private AdvisorUtils() {
	}

	public static Predicate<ChatClientResponse> onFinishReason() {
		return chatClientResponse -> {
			ChatResponse chatResponse = chatClientResponse.chatResponse();
			return chatResponse != null && chatResponse.getResults() != null
					&& chatResponse.getResults()
						.stream()
						.anyMatch(result -> result != null && result.getMetadata() != null
								&& StringUtils.hasText(result.getMetadata().getFinishReason()));
		};
	}

}
