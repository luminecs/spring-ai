package org.springframework.ai.minimax.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "MINIMAX_API_KEY", matches = ".+")
public class MiniMaxChatOptionsTests {

	private static final Logger logger = LoggerFactory.getLogger(MiniMaxChatOptionsTests.class);

	private final MiniMaxChatModel chatModel = new MiniMaxChatModel(new MiniMaxApi(System.getenv("MINIMAX_API_KEY")));

	@Test
	void testMarkSensitiveInfo() {

		UserMessage userMessage = new UserMessage(
				"Please extract the phone number, the content: My name is Bob, and my phone number is 133-12345678");

		List<Message> messages = new ArrayList<>(List.of(userMessage));

		ChatResponse response = this.chatModel.call(new Prompt(messages));
		String responseContent = response.getResult().getOutput().getText();

		assertThat(responseContent).contains("133-**");
		assertThat(responseContent).doesNotContain("133-12345678");

		var chatOptions = MiniMaxChatOptions.builder().maskSensitiveInfo(false).build();

		ChatResponse unmaskResponse = this.chatModel.call(new Prompt(messages, chatOptions));
		String unmaskResponseContent = unmaskResponse.getResult().getOutput().getText();

		assertThat(unmaskResponseContent).contains("133-12345678");
	}

	@Test
	void testWebSearch() {
		UserMessage userMessage = new UserMessage(
				"How many gold medals has the United States won in total at the 2024 Olympics?");

		List<Message> messages = new ArrayList<>(List.of(userMessage));

		List<MiniMaxApi.FunctionTool> functionTool = List.of(MiniMaxApi.FunctionTool.webSearchFunctionTool());

		MiniMaxChatOptions options = MiniMaxChatOptions.builder()
			.model(org.springframework.ai.minimax.api.MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.value)
			.tools(functionTool)
			.build();

		ChatResponse response = this.chatModel.call(new Prompt(messages, options));
		String responseContent = response.getResult().getOutput().getText();

		assertThat(responseContent).contains("40");
	}

	@Test
	void testWebSearchStream() {
		UserMessage userMessage = new UserMessage(
				"How many gold medals has the United States won in total at the 2024 Olympics?");

		List<Message> messages = new ArrayList<>(List.of(userMessage));

		List<MiniMaxApi.FunctionTool> functionTool = List.of(MiniMaxApi.FunctionTool.webSearchFunctionTool());

		MiniMaxChatOptions options = MiniMaxChatOptions.builder()
			.model(org.springframework.ai.minimax.api.MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.value)
			.tools(functionTool)
			.build();

		Flux<ChatResponse> response = this.chatModel.stream(new Prompt(messages, options));
		String content = Objects.requireNonNull(response.collectList().block())
			.stream()
			.map(ChatResponse::getResults)
			.flatMap(List::stream)
			.map(Generation::getOutput)
			.map(AssistantMessage::getText)
			.filter(Objects::nonNull)
			.collect(Collectors.joining());
		logger.info("Response: {}", content);

		assertThat(content).contains("40");
	}

}
