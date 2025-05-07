package org.springframework.ai.model.anthropic.autoconfigure;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.core.publisher.Flux;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".*")
public class AnthropicChatAutoConfigurationIT {

	private static final Log logger = LogFactory.getLog(AnthropicChatAutoConfigurationIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.anthropic.apiKey=" + System.getenv("ANTHROPIC_API_KEY"))
		.withConfiguration(AutoConfigurations.of(AnthropicChatAutoConfiguration.class));

	@Test
	void call() {
		this.contextRunner.run(context -> {
			AnthropicChatModel chatModel = context.getBean(AnthropicChatModel.class);
			String response = chatModel.call("Hello");
			assertThat(response).isNotEmpty();
			logger.info("Response: " + response);
		});
	}

	@Test
	void callWith8KResponseContext() {
		this.contextRunner
			.withPropertyValues("spring.ai.anthropic.beta-version=" + AnthropicApi.BETA_MAX_TOKENS,
					"spring.ai.anthropic.chat.options.model=" + AnthropicApi.ChatModel.CLAUDE_3_5_SONNET.getValue())
			.run(context -> {
				AnthropicChatModel chatModel = context.getBean(AnthropicChatModel.class);
				var options = AnthropicChatOptions.builder().maxTokens(8192).build();
				var response = chatModel.call(new Prompt("Tell me a joke", options));
				assertThat(response.getResult().getOutput().getText()).isNotEmpty();
				logger.info("Response: " + response);
			});
	}

	@Test
	void stream() {
		this.contextRunner.run(context -> {
			AnthropicChatModel chatModel = context.getBean(AnthropicChatModel.class);
			Flux<ChatResponse> responseFlux = chatModel.stream(new Prompt(new UserMessage("Hello")));

			String response = responseFlux.collectList()
				.block()
				.stream()
				.map(ChatResponse::getResults)
				.flatMap(List::stream)
				.map(Generation::getOutput)
				.map(AssistantMessage::getText)
				.collect(Collectors.joining());

			assertThat(response).isNotEmpty();
			logger.info("Response: " + response);
		});
	}

}
