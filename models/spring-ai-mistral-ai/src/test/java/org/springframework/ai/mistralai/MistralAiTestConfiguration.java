package org.springframework.ai.mistralai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.mistralai.api.MistralAiModerationApi;
import org.springframework.ai.mistralai.moderation.MistralAiModerationModel;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class MistralAiTestConfiguration {

	@Bean
	public MistralAiApi mistralAiApi() {
		var apiKey = System.getenv("MISTRAL_AI_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"Missing MISTRAL_AI_API_KEY environment variable. Please set it to your Mistral AI API key.");
		}
		return new MistralAiApi(apiKey);
	}

	@Bean
	public MistralAiModerationApi mistralAiModerationApi() {
		var apiKey = System.getenv("MISTRAL_AI_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"Missing MISTRAL_AI_API_KEY environment variable. Please set it to your Mistral AI API key.");
		}
		return new MistralAiModerationApi(apiKey);
	}

	@Bean
	public EmbeddingModel mistralAiEmbeddingModel(MistralAiApi api) {
		return new MistralAiEmbeddingModel(api,
				MistralAiEmbeddingOptions.builder().withModel(MistralAiApi.EmbeddingModel.EMBED.getValue()).build());
	}

	@Bean
	public MistralAiChatModel mistralAiChatModel(MistralAiApi mistralAiApi) {
		return MistralAiChatModel.builder()
			.mistralAiApi(mistralAiApi)
			.defaultOptions(MistralAiChatOptions.builder().model(MistralAiApi.ChatModel.SMALL.getValue()).build())
			.build();
	}

	@Bean
	public MistralAiModerationModel mistralAiModerationModel(MistralAiModerationApi mistralAiModerationApi) {
		return new MistralAiModerationModel(mistralAiModerationApi);
	}

}
