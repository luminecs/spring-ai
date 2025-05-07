package org.springframework.ai.model.chat.memory.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMemoryAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ChatMemoryAutoConfiguration.class));

	@Test
	void defaultConfiguration() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(ChatMemoryRepository.class);
			assertThat(context).hasSingleBean(ChatMemory.class);
		});
	}

	@Test
	void whenChatMemoryRepositoryExists() {
		contextRunner.withUserConfiguration(CustomChatMemoryRepositoryConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(ChatMemoryRepository.class);
			assertThat(context).hasBean("customChatMemoryRepository");
			assertThat(context).doesNotHaveBean("chatMemoryRepository");
		});
	}

	@Test
	void whenChatMemoryExists() {
		contextRunner.withUserConfiguration(CustomChatMemoryRepositoryConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(ChatMemoryRepository.class);
			assertThat(context).hasBean("customChatMemoryRepository");
			assertThat(context).doesNotHaveBean("chatMemoryRepository");
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomChatMemoryRepositoryConfiguration {

		private final ChatMemoryRepository customChatMemoryRepository = new InMemoryChatMemoryRepository();

		@Bean
		ChatMemoryRepository customChatMemoryRepository() {
			return customChatMemoryRepository;
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class CustomChatMemoryConfiguration {

		private final ChatMemory customChatMemory = MessageWindowChatMemory.builder().build();

		@Bean
		ChatMemory customChatMemory() {
			return customChatMemory;
		}

	}

}
