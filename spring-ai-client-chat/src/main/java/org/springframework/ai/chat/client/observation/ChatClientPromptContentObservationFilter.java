package org.springframework.ai.chat.client.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

public final class ChatClientPromptContentObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {
		if (!(context instanceof ChatClientObservationContext chatClientObservationContext)) {
			return context;
		}

		var prompts = processPrompt(chatClientObservationContext);

		chatClientObservationContext
			.addHighCardinalityKeyValue(ChatModelObservationDocumentation.HighCardinalityKeyNames.PROMPT
				.withValue(TracingHelper.concatenateMaps(prompts)));

		return chatClientObservationContext;
	}

	private Map<String, Object> processPrompt(ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().prompt().getInstructions())) {
			return Map.of();
		}

		var messages = new HashMap<String, Object>();
		context.getRequest()
			.prompt()
			.getInstructions()
			.forEach(message -> messages.put(message.getMessageType().getValue(), message.getText()));
		return messages;
	}

}
