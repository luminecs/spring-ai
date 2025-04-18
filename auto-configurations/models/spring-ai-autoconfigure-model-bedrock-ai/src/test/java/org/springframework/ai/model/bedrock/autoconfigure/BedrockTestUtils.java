package org.springframework.ai.model.bedrock.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public final class BedrockTestUtils {

	private BedrockTestUtils() {
	}

	public static ApplicationContextRunner getContextRunner() {
		return new ApplicationContextRunner()
			.withPropertyValues("spring.ai.bedrock.aws.access-key=" + System.getenv("AWS_ACCESS_KEY_ID"),
					"spring.ai.bedrock.aws.secret-key=" + System.getenv("AWS_SECRET_ACCESS_KEY"),
					"spring.ai.bedrock.aws.session-token=" + System.getenv("AWS_SESSION_TOKEN"),
					"spring.ai.bedrock.aws.region=" + Region.US_EAST_1.id())
			.withUserConfiguration(Config.class);
	}

	public static ApplicationContextRunner getContextRunnerWithUserConfiguration() {
		return new ApplicationContextRunner().withUserConfiguration(Config.class);
	}

	@Configuration
	static class Config {

		@Bean
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

	}

}
