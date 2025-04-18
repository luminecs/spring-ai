package org.springframework.ai.chat.client.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;

import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class ChatClientInputContentObservationFilter implements ObservationFilter {

	@Override
	public Observation.Context map(Observation.Context context) {
		if (!(context instanceof ChatClientObservationContext chatClientObservationContext)) {
			return context;
		}

		chatClientSystemText(chatClientObservationContext);
		chatClientSystemParams(chatClientObservationContext);
		chatClientUserText(chatClientObservationContext);
		chatClientUserParams(chatClientObservationContext);

		return chatClientObservationContext;
	}

	protected void chatClientSystemText(ChatClientObservationContext context) {
		if (!StringUtils.hasText(context.getRequest().getSystemText())) {
			return;
		}
		context.addHighCardinalityKeyValue(
				KeyValue.of(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_SYSTEM_TEXT,
						context.getRequest().getSystemText()));
	}

	protected void chatClientSystemParams(ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().getSystemParams())) {
			return;
		}
		context.addHighCardinalityKeyValue(
				KeyValue.of(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_SYSTEM_PARAM,
						TracingHelper.concatenateMaps(context.getRequest().getSystemParams())));
	}

	protected void chatClientUserText(ChatClientObservationContext context) {
		if (!StringUtils.hasText(context.getRequest().getUserText())) {
			return;
		}
		context.addHighCardinalityKeyValue(
				KeyValue.of(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_USER_TEXT,
						context.getRequest().getUserText()));
	}

	protected void chatClientUserParams(ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().getUserParams())) {
			return;
		}
		context.addHighCardinalityKeyValue(
				KeyValue.of(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_USER_PARAMS,
						TracingHelper.concatenateMaps(context.getRequest().getUserParams())));
	}

}
