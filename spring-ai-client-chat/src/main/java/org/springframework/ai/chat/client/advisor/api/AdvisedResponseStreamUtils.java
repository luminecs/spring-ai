package org.springframework.ai.chat.client.advisor.api;

import java.util.function.Predicate;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;

public final class AdvisedResponseStreamUtils {

	private AdvisedResponseStreamUtils() {

	}

	public static Predicate<AdvisedResponse> onFinishReason() {
		return advisedResponse -> {
			ChatResponse chatResponse = advisedResponse.response();
			return chatResponse != null && chatResponse.getResults() != null
					&& chatResponse.getResults()
						.stream()
						.anyMatch(result -> result != null && result.getMetadata() != null
								&& StringUtils.hasText(result.getMetadata().getFinishReason()));
		};
	}

}
