package org.springframework.ai.model.openai.autoconfigure;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
public class ChatClientAutoConfigurationIT {

	private static final Log logger = LogFactory.getLog(ChatClientAutoConfigurationIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.openai.apiKey=" + System.getenv("OPENAI_API_KEY"),
				"spring.ai.openai.chat.options.model=gpt-4o")
		.withConfiguration(
				AutoConfigurations.of(SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class,
						OpenAiChatAutoConfiguration.class, ChatClientAutoConfiguration.class));

	@Test
	void implicitlyEnabled() {
		this.contextRunner.run(context -> assertThat(context.getBeansOfType(ChatClient.Builder.class)).isNotEmpty());
	}

	@Test
	void explicitlyEnabled() {
		this.contextRunner.withPropertyValues("spring.ai.chat.client.enabled=true")
			.run(context -> assertThat(context.getBeansOfType(ChatClient.Builder.class)).isNotEmpty());
	}

	@Test
	void explicitlyDisabled() {
		this.contextRunner.withPropertyValues("spring.ai.chat.client.enabled=false")
			.run(context -> assertThat(context.getBeansOfType(ChatClient.Builder.class)).isEmpty());
	}

	@Test
	void generate() {
		this.contextRunner.run(context -> {
			ChatClient.Builder builder = context.getBean(ChatClient.Builder.class);

			assertThat(builder).isNotNull();

			ChatClient chatClient = builder.build();

			String response = chatClient.prompt().user("Hello").call().content();

			assertThat(response).isNotEmpty();
			logger.info("Response: " + response);
		});
	}

	@Test
	void testChatClientCustomizers() {
		this.contextRunner.withUserConfiguration(Config.class).run(context -> {

			ChatClient.Builder builder = context.getBean(ChatClient.Builder.class);

			ChatClient chatClient = builder.build();

			assertThat(chatClient).isNotNull();

			ActorsFilms actorsFilms = chatClient.prompt()
				.user(u -> u.param("actor", "Tom Hanks"))
				.call()
				.entity(ActorsFilms.class);

			logger.info("" + actorsFilms);
			assertThat(actorsFilms.actor()).isEqualTo("Tom Hanks");
			assertThat(actorsFilms.movies()).hasSize(5);
		});
	}

	record ActorsFilms(String actor, List<String> movies) {

	}

	@Configuration
	static class Config {

		@Bean
		public ChatClientCustomizer chatClientCustomizer() {
			return b -> b.defaultSystem("You are a movie expert.")
				.defaultUser("Generate the filmography of 5 movies for {actor}.");
		}

	}

}
