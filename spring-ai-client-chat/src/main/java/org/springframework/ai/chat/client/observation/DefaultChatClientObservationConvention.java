package org.springframework.ai.chat.client.observation;

import java.util.ArrayList;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationDocumentation.LowCardinalityKeyNames;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.observation.conventions.SpringAiKind;
import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class DefaultChatClientObservationConvention implements ChatClientObservationConvention {

	public static final String DEFAULT_NAME = "spring.ai.chat.client";

	private final String name;

	public DefaultChatClientObservationConvention() {
		this(DEFAULT_NAME);
	}

	public DefaultChatClientObservationConvention(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	@Nullable
	public String getContextualName(ChatClientObservationContext context) {
		return "%s %s".formatted(context.getOperationMetadata().provider(), SpringAiKind.CHAT_CLIENT.value());
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(ChatClientObservationContext context) {
		return KeyValues.of(aiOperationType(context), aiProvider(context), springAiKind(), stream(context));
	}

	protected KeyValue aiOperationType(ChatClientObservationContext context) {
		return KeyValue.of(ChatModelObservationDocumentation.LowCardinalityKeyNames.AI_OPERATION_TYPE,
				context.getOperationMetadata().operationType());
	}

	protected KeyValue aiProvider(ChatClientObservationContext context) {
		return KeyValue.of(ChatModelObservationDocumentation.LowCardinalityKeyNames.AI_PROVIDER,
				context.getOperationMetadata().provider());
	}

	protected KeyValue springAiKind() {
		return KeyValue.of(ChatClientObservationDocumentation.LowCardinalityKeyNames.SPRING_AI_KIND,
				SpringAiKind.CHAT_CLIENT.value());
	}

	protected KeyValue stream(ChatClientObservationContext context) {
		return KeyValue.of(LowCardinalityKeyNames.STREAM, "" + context.isStream());
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(ChatClientObservationContext context) {
		var keyValues = KeyValues.empty();
		keyValues = advisors(keyValues, context);
		keyValues = conversationId(keyValues, context);
		keyValues = tools(keyValues, context);
		return keyValues;
	}

	protected KeyValues advisors(KeyValues keyValues, ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getAdvisors())) {
			return keyValues;
		}
		var advisorNames = context.getAdvisors().stream().map(Advisor::getName).toList();
		return keyValues.and(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_ADVISORS.asString(),
				TracingHelper.concatenateStrings(advisorNames));
	}

	protected KeyValues conversationId(KeyValues keyValues, ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().context())) {
			return keyValues;
		}

		var conversationIdValue = context.getRequest()
			.context()
			.get(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY);

		if (!(conversationIdValue instanceof String conversationId) || !StringUtils.hasText(conversationId)) {
			return keyValues;
		}

		return keyValues.and(
				ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_CONVERSATION_ID.asString(),
				conversationId);
	}

	protected KeyValues tools(KeyValues keyValues, ChatClientObservationContext context) {
		if (context.getRequest().prompt().getOptions() == null) {
			return keyValues;
		}
		if (!(context.getRequest().prompt().getOptions() instanceof ToolCallingChatOptions options)) {
			return keyValues;
		}

		var toolNames = new ArrayList<>(options.getToolNames());
		var toolCallbacks = options.getToolCallbacks();

		if (CollectionUtils.isEmpty(toolNames) && CollectionUtils.isEmpty(toolCallbacks)) {
			return keyValues;
		}

		toolCallbacks.forEach(toolCallback -> toolNames.add(toolCallback.getToolDefinition().name()));

		return keyValues.and(
				ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_TOOL_NAMES.asString(),
				TracingHelper.concatenateStrings(toolNames.stream().sorted().toList()));
	}

}
