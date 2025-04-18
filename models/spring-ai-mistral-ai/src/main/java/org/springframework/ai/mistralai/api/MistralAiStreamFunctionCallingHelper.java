package org.springframework.ai.mistralai.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionChunk;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionChunk.ChunkChoice;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionFinishReason;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionMessage;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionMessage.ChatCompletionFunction;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionMessage.ToolCall;
import org.springframework.ai.mistralai.api.MistralAiApi.LogProbs;
import org.springframework.util.CollectionUtils;

public class MistralAiStreamFunctionCallingHelper {

	public ChatCompletionChunk merge(ChatCompletionChunk previous, ChatCompletionChunk current) {

		if (previous == null) {
			return current;
		}

		String id = (current.id() != null ? current.id() : previous.id());
		Long created = (current.created() != null ? current.created() : previous.created());
		String model = (current.model() != null ? current.model() : previous.model());
		String object = (current.object() != null ? current.object() : previous.object());

		ChunkChoice previousChoice0 = (CollectionUtils.isEmpty(previous.choices()) ? null : previous.choices().get(0));
		ChunkChoice currentChoice0 = (CollectionUtils.isEmpty(current.choices()) ? null : current.choices().get(0));

		ChunkChoice choice = merge(previousChoice0, currentChoice0);

		MistralAiApi.Usage usage = (current.usage() != null ? current.usage() : previous.usage());

		return new ChatCompletionChunk(id, object, created, model, List.of(choice), usage);
	}

	private ChunkChoice merge(ChunkChoice previous, ChunkChoice current) {
		if (previous == null) {
			if (current.delta() != null && current.delta().toolCalls() != null) {
				Optional<String> id = current.delta()
					.toolCalls()
					.stream()
					.map(ToolCall::id)
					.filter(Objects::nonNull)
					.findFirst();
				if (id.isEmpty()) {
					var newId = UUID.randomUUID().toString();

					var toolCallsWithID = current.delta()
						.toolCalls()
						.stream()
						.map(toolCall -> new ToolCall(newId, "function", toolCall.function(), toolCall.index()))
						.toList();

					var role = current.delta().role() != null ? current.delta().role() : Role.ASSISTANT;
					current = new ChunkChoice(
							current.index(), new ChatCompletionMessage(current.delta().content(), role,
									current.delta().name(), toolCallsWithID),
							current.finishReason(), current.logprobs());
				}
			}
			return current;
		}

		ChatCompletionFinishReason finishReason = (current.finishReason() != null ? current.finishReason()
				: previous.finishReason());
		Integer index = (current.index() != null ? current.index() : previous.index());

		ChatCompletionMessage message = merge(previous.delta(), current.delta());
		LogProbs logprobs = (current.logprobs() != null ? current.logprobs() : previous.logprobs());

		return new ChunkChoice(index, message, finishReason, logprobs);
	}

	private ChatCompletionMessage merge(ChatCompletionMessage previous, ChatCompletionMessage current) {
		String content = (current.content() != null ? current.content()
				: (previous.content() != null) ? previous.content() : "");
		Role role = (current.role() != null ? current.role() : previous.role());
		role = (role != null ? role : Role.ASSISTANT);
		String name = (current.name() != null ? current.name() : previous.name());

		List<ToolCall> toolCalls = new ArrayList<>();
		ToolCall lastPreviousTooCall = null;
		if (previous.toolCalls() != null) {
			lastPreviousTooCall = previous.toolCalls().get(previous.toolCalls().size() - 1);
			if (previous.toolCalls().size() > 1) {
				toolCalls.addAll(previous.toolCalls().subList(0, previous.toolCalls().size() - 1));
			}
		}
		if (current.toolCalls() != null) {
			if (current.toolCalls().size() > 1) {
				throw new IllegalStateException("Currently only one tool call is supported per message!");
			}
			var currentToolCall = current.toolCalls().iterator().next();
			if (currentToolCall.id() != null) {
				if (lastPreviousTooCall != null) {
					toolCalls.add(lastPreviousTooCall);
				}
				toolCalls.add(currentToolCall);
			}
			else {
				toolCalls.add(merge(lastPreviousTooCall, currentToolCall));
			}
		}
		else {
			if (lastPreviousTooCall != null) {
				toolCalls.add(lastPreviousTooCall);
			}
		}
		return new ChatCompletionMessage(content, role, name, toolCalls);
	}

	private ToolCall merge(ToolCall previous, ToolCall current) {
		if (previous == null) {
			return current;
		}
		String id = (current.id() != null ? current.id() : previous.id());
		String type = (current.type() != null ? current.type() : previous.type());
		ChatCompletionFunction function = merge(previous.function(), current.function());
		Integer index = (current.index() != null ? current.index() : previous.index());
		return new ToolCall(id, type, function, index);
	}

	private ChatCompletionFunction merge(ChatCompletionFunction previous, ChatCompletionFunction current) {
		if (previous == null) {
			return current;
		}
		String name = (current.name() != null ? current.name() : previous.name());
		StringBuilder arguments = new StringBuilder();
		if (previous.arguments() != null) {
			arguments.append(previous.arguments());
		}
		if (current.arguments() != null) {
			arguments.append(current.arguments());
		}
		return new ChatCompletionFunction(name, arguments.toString());
	}

	public boolean isStreamingToolFunctionCall(ChatCompletionChunk chatCompletion) {

		var choices = chatCompletion.choices();
		if (CollectionUtils.isEmpty(choices)) {
			return false;
		}

		var choice = choices.get(0);
		return !CollectionUtils.isEmpty(choice.delta().toolCalls());
	}

	public boolean isStreamingToolFunctionCallFinish(ChatCompletionChunk chatCompletion) {

		var choices = chatCompletion.choices();
		if (CollectionUtils.isEmpty(choices)) {
			return false;
		}

		var choice = choices.get(0);
		return choice.finishReason() == ChatCompletionFinishReason.TOOL_CALLS;
	}

}
