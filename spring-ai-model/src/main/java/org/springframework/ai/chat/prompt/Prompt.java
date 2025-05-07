package org.springframework.ai.chat.prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.ModelRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class Prompt implements ModelRequest<List<Message>> {

	private final List<Message> messages;

	@Nullable
	private ChatOptions chatOptions;

	public Prompt(String contents) {
		this(new UserMessage(contents));
	}

	public Prompt(Message message) {
		this(Collections.singletonList(message));
	}

	public Prompt(List<Message> messages) {
		this(messages, null);
	}

	public Prompt(Message... messages) {
		this(Arrays.asList(messages), null);
	}

	public Prompt(String contents, @Nullable ChatOptions chatOptions) {
		this(new UserMessage(contents), chatOptions);
	}

	public Prompt(Message message, @Nullable ChatOptions chatOptions) {
		this(Collections.singletonList(message), chatOptions);
	}

	public Prompt(List<Message> messages, @Nullable ChatOptions chatOptions) {
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");
		this.messages = messages;
		this.chatOptions = chatOptions;
	}

	public String getContents() {
		StringBuilder sb = new StringBuilder();
		for (Message message : getInstructions()) {
			sb.append(message.getText());
		}
		return sb.toString();
	}

	@Override
	@Nullable
	public ChatOptions getOptions() {
		return this.chatOptions;
	}

	@Override
	public List<Message> getInstructions() {
		return this.messages;
	}

	public SystemMessage getSystemMessage() {
		for (int i = 0; i <= this.messages.size() - 1; i++) {
			Message message = this.messages.get(i);
			if (message instanceof SystemMessage systemMessage) {
				return systemMessage;
			}
		}
		return new SystemMessage("");
	}

	public UserMessage getUserMessage() {
		for (int i = this.messages.size() - 1; i >= 0; i--) {
			Message message = this.messages.get(i);
			if (message instanceof UserMessage userMessage) {
				return userMessage;
			}
		}
		return new UserMessage("");
	}

	@Override
	public String toString() {
		return "Prompt{" + "messages=" + this.messages + ", modelOptions=" + this.chatOptions + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Prompt prompt)) {
			return false;
		}
		return Objects.equals(this.messages, prompt.messages) && Objects.equals(this.chatOptions, prompt.chatOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messages, this.chatOptions);
	}

	public Prompt copy() {
		return new Prompt(instructionsCopy(), null == this.chatOptions ? null : this.chatOptions.copy());
	}

	private List<Message> instructionsCopy() {
		List<Message> messagesCopy = new ArrayList<>();
		this.messages.forEach(message -> {
			if (message instanceof UserMessage userMessage) {
				messagesCopy.add(userMessage.copy());
			}
			else if (message instanceof SystemMessage systemMessage) {
				messagesCopy.add(systemMessage.copy());
			}
			else if (message instanceof AssistantMessage assistantMessage) {
				messagesCopy.add(new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(),
						assistantMessage.getToolCalls()));
			}
			else if (message instanceof ToolResponseMessage toolResponseMessage) {
				messagesCopy.add(new ToolResponseMessage(new ArrayList<>(toolResponseMessage.getResponses()),
						new HashMap<>(toolResponseMessage.getMetadata())));
			}
			else {
				throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
			}
		});

		return messagesCopy;
	}

	public Prompt augmentSystemMessage(Function<SystemMessage, SystemMessage> systemMessageAugmenter) {

		var messagesCopy = new ArrayList<>(this.messages);
		for (int i = 0; i <= this.messages.size() - 1; i++) {
			Message message = messagesCopy.get(i);
			if (message instanceof SystemMessage systemMessage) {
				messagesCopy.set(i, systemMessageAugmenter.apply(systemMessage));
				break;
			}
			if (i == 0) {

				messagesCopy.add(0, systemMessageAugmenter.apply(new SystemMessage("")));
			}
		}

		return new Prompt(messagesCopy, null == this.chatOptions ? null : this.chatOptions.copy());
	}

	public Prompt augmentSystemMessage(String newSystemText) {
		return augmentSystemMessage(systemMessage -> systemMessage.mutate().text(newSystemText).build());
	}

	public Prompt augmentUserMessage(Function<UserMessage, UserMessage> userMessageAugmenter) {
		var messagesCopy = new ArrayList<>(this.messages);
		for (int i = messagesCopy.size() - 1; i >= 0; i--) {
			Message message = messagesCopy.get(i);
			if (message instanceof UserMessage userMessage) {
				messagesCopy.set(i, userMessageAugmenter.apply(userMessage));
				break;
			}
			if (i == 0) {
				messagesCopy.add(userMessageAugmenter.apply(new UserMessage("")));
			}
		}

		return new Prompt(messagesCopy, null == this.chatOptions ? null : this.chatOptions.copy());
	}

	public Prompt augmentUserMessage(String newUserText) {
		return augmentUserMessage(userMessage -> userMessage.mutate().text(newUserText).build());
	}

	public Builder mutate() {
		Builder builder = new Builder().messages(instructionsCopy());
		if (this.chatOptions != null) {
			builder.chatOptions(this.chatOptions.copy());
		}
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		@Nullable
		private String content;

		@Nullable
		private List<Message> messages;

		@Nullable
		private ChatOptions chatOptions;

		public Builder content(@Nullable String content) {
			this.content = content;
			return this;
		}

		public Builder messages(Message... messages) {
			if (messages != null) {
				this.messages = Arrays.asList(messages);
			}
			return this;
		}

		public Builder messages(List<Message> messages) {
			this.messages = messages;
			return this;
		}

		public Builder chatOptions(ChatOptions chatOptions) {
			this.chatOptions = chatOptions;
			return this;
		}

		public Prompt build() {
			if (StringUtils.hasText(this.content) && !CollectionUtils.isEmpty(this.messages)) {
				throw new IllegalArgumentException("content and messages cannot be set at the same time");
			}
			else if (StringUtils.hasText(this.content)) {
				this.messages = List.of(new UserMessage(this.content));
			}
			return new Prompt(this.messages, this.chatOptions);
		}

	}

}
