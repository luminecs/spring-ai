package org.springframework.ai.aot;

import java.util.Set;

import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class SpringAiCoreRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {

		var chatTypes = Set.of(AbstractMessage.class, AssistantMessage.class, ToolResponseMessage.class, Message.class,
				MessageType.class, UserMessage.class, SystemMessage.class);
		for (var c : chatTypes) {
			hints.reflection().registerType(c);
		}

		// Register tool-related types for reflection
		var toolTypes = Set.of(ToolCallback.class, ToolDefinition.class);
		for (var c : toolTypes) {
			hints.reflection().registerType(c);
		}

		for (var r : Set.of("embedding/embedding-model-dimensions.properties")) {
			hints.resources().registerResource(new ClassPathResource(r));
		}

	}

}
