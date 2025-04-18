package org.springframework.ai.minimax;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class MiniMaxTestConfiguration {

	@Bean
	public MiniMaxApi miniMaxApi() {
		return new MiniMaxApi(getApiKey());
	}

	private String getApiKey() {
		String apiKey = System.getenv("MINIMAX_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key. Put it in an environment variable under the name MINIMAX_API_KEY");
		}
		return apiKey;
	}

	@Bean
	public MiniMaxChatModel miniMaxChatModel(MiniMaxApi api) {
		return new MiniMaxChatModel(api);
	}

	@Bean
	public EmbeddingModel miniMaxEmbeddingModel(MiniMaxApi api) {
		return new MiniMaxEmbeddingModel(api);
	}

}
