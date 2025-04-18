package org.springframework.ai.chat.client.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.ai.chat.client.ChatClientAttributes;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationDocumentation.LowCardinalityKeyNames;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.observation.conventions.SpringAiKind;
import org.springframework.ai.observation.tracing.TracingHelper;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

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
		keyValues = chatClientAdvisorNames(keyValues, context);

		keyValues = chatClientAdvisorParams(keyValues, context);

		keyValues = toolNames(keyValues, context);

		keyValues = toolCallbacks(keyValues, context);
		return keyValues;
	}

	@SuppressWarnings("unchecked")
	protected KeyValues chatClientAdvisorNames(KeyValues keyValues, ChatClientObservationContext context) {
		if (!(context.getRequest().context().get(ChatClientAttributes.ADVISORS.getKey()) instanceof List<?> advisors)) {
			return keyValues;
		}
		var advisorNames = ((List<Advisor>) advisors).stream().map(Advisor::getName).toList();
		return keyValues.and(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_ADVISORS.asString(),
				TracingHelper.concatenateStrings(advisorNames));
	}

	protected KeyValues chatClientAdvisorParams(KeyValues keyValues, ChatClientObservationContext context) {
		if (CollectionUtils.isEmpty(context.getRequest().context())) {
			return keyValues;
		}
		var chatClientContext = context.getRequest().context();
		Arrays.stream(ChatClientAttributes.values()).forEach(attribute -> chatClientContext.remove(attribute.getKey()));
		return keyValues.and(
				ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_ADVISOR_PARAMS.asString(),
				TracingHelper.concatenateMaps(chatClientContext));
	}

	protected KeyValues toolNames(KeyValues keyValues, ChatClientObservationContext context) {
		if (context.getRequest().prompt().getOptions() == null) {
			return keyValues;
		}
		if (!(context.getRequest().prompt().getOptions() instanceof ToolCallingChatOptions options)) {
			return keyValues;
		}

		var toolNames = options.getToolNames();
		if (CollectionUtils.isEmpty(toolNames)) {
			return keyValues;
		}

		return keyValues.and(
				ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_TOOL_FUNCTION_NAMES.asString(),
				TracingHelper.concatenateStrings(toolNames.stream().sorted().toList()));
	}

	protected KeyValues toolCallbacks(KeyValues keyValues, ChatClientObservationContext context) {
		if (context.getRequest().prompt().getOptions() == null) {
			return keyValues;
		}
		if (!(context.getRequest().prompt().getOptions() instanceof ToolCallingChatOptions options)) {
			return keyValues;
		}

		var toolCallbacks = options.getToolCallbacks();
		if (CollectionUtils.isEmpty(toolCallbacks)) {
			return keyValues;
		}

		var toolCallbackNames = toolCallbacks.stream().map(FunctionCallback::getName).sorted().toList();
		return keyValues
			.and(ChatClientObservationDocumentation.HighCardinalityKeyNames.CHAT_CLIENT_TOOL_FUNCTION_CALLBACKS
				.asString(), TracingHelper.concatenateStrings(toolCallbackNames));
	}

}
