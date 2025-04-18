package org.springframework.ai.chat.metadata;

import org.springframework.ai.chat.model.ChatResponse;

public final class UsageUtils {

	private UsageUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static Usage getCumulativeUsage(final Usage currentUsage, final ChatResponse previousChatResponse) {
		Usage usageFromPreviousChatResponse = null;
		if (previousChatResponse != null && previousChatResponse.getMetadata() != null
				&& previousChatResponse.getMetadata().getUsage() != null) {
			usageFromPreviousChatResponse = previousChatResponse.getMetadata().getUsage();
		}
		else {

			return currentUsage;
		}

		if (!isEmpty(currentUsage)) {
			Integer promptTokens = currentUsage.getPromptTokens();
			Integer generationTokens = currentUsage.getCompletionTokens();
			Integer totalTokens = currentUsage.getTotalTokens();

			promptTokens += usageFromPreviousChatResponse.getPromptTokens();
			generationTokens += usageFromPreviousChatResponse.getCompletionTokens();
			totalTokens += usageFromPreviousChatResponse.getTotalTokens();
			return new DefaultUsage(promptTokens, generationTokens, totalTokens);
		}

		return usageFromPreviousChatResponse;
	}

	public static boolean isEmpty(Usage usage) {
		if (usage == null) {
			return true;
		}
		else if (usage != null && usage.getTotalTokens() == 0L) {
			return true;
		}
		return false;
	}

}
