package org.springframework.ai.azure.openai;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.azure.ai.openai.models.AzureChatExtensionsMessageContext;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatChoiceLogProbabilityInfo;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.ContentFilterResultsForChoice;
import com.azure.ai.openai.models.ContentFilterResultsForPrompt;
import com.azure.ai.openai.models.FunctionCall;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public final class MergeUtils {

	private static final Class<?>[] CHAT_COMPLETIONS_CONSTRUCTOR_ARG_TYPES = new Class<?>[] { String.class,
			OffsetDateTime.class, List.class };

	private static final Class<?>[] chatChoiceConstructorArgumentTypes = new Class<?>[] {
			ChatChoiceLogProbabilityInfo.class, int.class, CompletionsFinishReason.class };

	private static final Class<?>[] chatResponseMessageConstructorArgumentTypes = new Class<?>[] { ChatRole.class,
			String.class, String.class };

	private MergeUtils() {

	}

	private static <T> T newInstance(Class<?>[] argumentTypes, Class<T> clazz, Object... args) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(argumentTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void setField(Object classInstance, String fieldName, Object fieldValue) {
		try {
			Field field = classInstance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(classInstance, fieldValue);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ChatCompletions emptyChatCompletions() {
		String id = null;
		List<ChatChoice> choices = new ArrayList<>();
		OffsetDateTime createdAt = OffsetDateTime.now();
		ChatCompletions chatCompletionsInstance = newInstance(CHAT_COMPLETIONS_CONSTRUCTOR_ARG_TYPES,
				ChatCompletions.class, id, createdAt, choices);
		List<ContentFilterResultsForPrompt> promptFilterResults = new ArrayList<>();
		setField(chatCompletionsInstance, "promptFilterResults", promptFilterResults);
		String systemFingerprint = null;
		setField(chatCompletionsInstance, "systemFingerprint", systemFingerprint);

		return chatCompletionsInstance;
	}

	public static ChatCompletions mergeChatCompletions(ChatCompletions left, ChatCompletions right) {

		Assert.isTrue(left != null, "");
		if (right == null) {
			Assert.isTrue(left.getId() != null, "");
			return left;
		}
		Assert.isTrue(left.getId() != null || right.getId() != null, "");

		String id = left.getId() != null ? left.getId() : right.getId();

		List<ChatChoice> choices = null;
		if (right.getChoices() == null) {
			choices = left.getChoices();
		}
		else {
			if (CollectionUtils.isEmpty(left.getChoices())) {
				choices = right.getChoices();
			}
			else {
				choices = List.of(mergeChatChoice(left.getChoices().get(0), right.getChoices().get(0)));
			}
		}

		CompletionsUsage usage = right.getUsage() == null ? left.getUsage() : right.getUsage();

		OffsetDateTime createdAt = left.getCreatedAt().isAfter(right.getCreatedAt()) ? left.getCreatedAt()
				: right.getCreatedAt();

		ChatCompletions instance = newInstance(CHAT_COMPLETIONS_CONSTRUCTOR_ARG_TYPES, ChatCompletions.class, id,
				createdAt, choices);

		List<ContentFilterResultsForPrompt> promptFilterResults = right.getPromptFilterResults() == null
				? left.getPromptFilterResults() : right.getPromptFilterResults();
		setField(instance, "promptFilterResults", promptFilterResults);

		String systemFingerprint = right.getSystemFingerprint() == null ? left.getSystemFingerprint()
				: right.getSystemFingerprint();
		setField(instance, "systemFingerprint", systemFingerprint);

		setField(instance, "usage", usage);

		return instance;
	}

	private static ChatChoice mergeChatChoice(ChatChoice left, ChatChoice right) {

		int index = Math.max(left.getIndex(), right.getIndex());

		CompletionsFinishReason finishReason = left.getFinishReason() != null ? left.getFinishReason()
				: right.getFinishReason();

		var logprobs = left.getLogprobs() != null ? left.getLogprobs() : right.getLogprobs();

		final ChatChoice instance = newInstance(chatChoiceConstructorArgumentTypes, ChatChoice.class, logprobs, index,
				finishReason);

		ChatResponseMessage message = null;
		if (left.getMessage() == null) {
			message = right.getMessage();
		}
		else {
			message = mergeChatResponseMessage(left.getMessage(), right.getMessage());
		}

		setField(instance, "message", message);

		ChatResponseMessage delta = null;
		if (left.getDelta() == null) {
			delta = right.getDelta();
		}
		else {
			delta = mergeChatResponseMessage(left.getDelta(), right.getDelta());
		}
		setField(instance, "delta", delta);

		ContentFilterResultsForChoice contentFilterResults = left.getContentFilterResults() != null
				? left.getContentFilterResults() : right.getContentFilterResults();
		setField(instance, "contentFilterResults", contentFilterResults);

		var enhancements = left.getEnhancements() != null ? left.getEnhancements() : right.getEnhancements();
		setField(instance, "enhancements", enhancements);

		return instance;
	}

	private static ChatResponseMessage mergeChatResponseMessage(ChatResponseMessage left, ChatResponseMessage right) {

		var role = left.getRole() != null ? left.getRole() : right.getRole();
		String content = null;
		if (left.getContent() != null && right.getContent() != null) {
			content = left.getContent().concat(right.getContent());
		}
		else if (left.getContent() == null) {
			content = right.getContent();
		}
		else {
			content = left.getContent();
		}

		String refusal = left.getRefusal() != null ? left.getRefusal() : right.getRefusal();

		ChatResponseMessage instance = newInstance(chatResponseMessageConstructorArgumentTypes,
				ChatResponseMessage.class, role, refusal, content);

		List<ChatCompletionsToolCall> toolCalls = new ArrayList<>();
		if (left.getToolCalls() == null) {
			if (right.getToolCalls() != null) {
				toolCalls.addAll(right.getToolCalls());
			}
		}
		else if (right.getToolCalls() == null) {
			toolCalls.addAll(left.getToolCalls());
		}
		else {
			toolCalls.addAll(left.getToolCalls());
			final var lastToolIndex = toolCalls.size() - 1;
			ChatCompletionsToolCall lastTool = toolCalls.get(lastToolIndex);
			if (right.getToolCalls().get(0).getId() == null) {

				lastTool = mergeChatCompletionsToolCall(lastTool, right.getToolCalls().get(0));

				toolCalls.remove(lastToolIndex);
				toolCalls.add(lastTool);
			}
			else {
				toolCalls.add(right.getToolCalls().get(0));
			}
		}

		setField(instance, "toolCalls", toolCalls);

		FunctionCall functionCall = null;

		if (left.getFunctionCall() == null) {
			functionCall = right.getFunctionCall();
		}
		else {
			functionCall = MergeUtils.mergeFunctionCall(left.getFunctionCall(), right.getFunctionCall());
		}

		setField(instance, "functionCall", functionCall);

		AzureChatExtensionsMessageContext context = left.getContext() != null ? left.getContext() : right.getContext();
		setField(instance, "context", context);

		return instance;
	}

	private static ChatCompletionsToolCall mergeChatCompletionsToolCall(ChatCompletionsToolCall left,
			ChatCompletionsToolCall right) {
		Assert.isTrue(Objects.equals(left.getType(), right.getType()),
				"Cannot merge different type of AccessibleChatCompletionsToolCall");
		if (!"function".equals(left.getType())) {
			throw new UnsupportedOperationException("Only function chat completion tool is supported");
		}

		String id = left.getId() != null ? left.getId() : right.getId();
		var mergedFunction = mergeFunctionCall(((ChatCompletionsFunctionToolCall) left).getFunction(),
				((ChatCompletionsFunctionToolCall) right).getFunction());

		return new ChatCompletionsFunctionToolCall(id, mergedFunction);
	}

	private static FunctionCall mergeFunctionCall(FunctionCall left, FunctionCall right) {
		var name = left.getName() != null ? left.getName() : right.getName();
		String arguments = null;
		if (left.getArguments() != null && right.getArguments() != null) {
			arguments = left.getArguments() + right.getArguments();
		}
		else if (left.getArguments() == null) {
			arguments = right.getArguments();
		}
		else {
			arguments = left.getArguments();
		}
		return new FunctionCall(name, arguments);
	}

}
