package org.springframework.ai.anthropic;

import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class AnthropicTestConfiguration {

	@Bean
	public AnthropicApi anthropicApi() {
		return new AnthropicApi(getApiKey());
	}

	private String getApiKey() {
		String apiKey = System.getenv("ANTHROPIC_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key.  Put it in an environment variable under the name ANTHROPIC_API_KEY");
		}
		return apiKey;
	}

	@Bean
	public AnthropicChatModel anthropicChatModel(AnthropicApi api) {
		return AnthropicChatModel.builder().anthropicApi(api).build();
	}

}
