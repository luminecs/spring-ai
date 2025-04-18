package org.springframework.ai.chat.observation;

import java.util.List;

import org.springframework.ai.content.Content;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public final class ChatModelObservationContentProcessor {

	private ChatModelObservationContentProcessor() {
	}

	public static List<String> prompt(ChatModelObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().getInstructions())) {
			return List.of();
		}

		return context.getRequest().getInstructions().stream().map(Content::getText).toList();
	}

	public static List<String> completion(ChatModelObservationContext context) {
		if (context == null || context.getResponse() == null || context.getResponse().getResults() == null
				|| CollectionUtils.isEmpty(context.getResponse().getResults())) {
			return List.of();
		}

		if (!StringUtils.hasText(context.getResponse().getResult().getOutput().getText())) {
			return List.of();
		}

		return context.getResponse()
			.getResults()
			.stream()
			.filter(generation -> generation.getOutput() != null
					&& StringUtils.hasText(generation.getOutput().getText()))
			.map(generation -> generation.getOutput().getText())
			.toList();
	}

}
