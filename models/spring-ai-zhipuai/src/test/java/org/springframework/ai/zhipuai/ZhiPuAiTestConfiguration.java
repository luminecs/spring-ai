package org.springframework.ai.zhipuai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class ZhiPuAiTestConfiguration {

	@Bean
	public ZhiPuAiApi zhiPuAiApi() {
		return new ZhiPuAiApi(getApiKey());
	}

	@Bean
	public ZhiPuAiImageApi zhiPuAiImageApi() {
		return new ZhiPuAiImageApi(getApiKey());
	}

	private String getApiKey() {
		String apiKey = System.getenv("ZHIPU_AI_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key.  Put it in an environment variable under the name ZHIPU_AI_API_KEY");
		}
		return apiKey;
	}

	@Bean
	public ZhiPuAiChatModel zhiPuAiChatModel(ZhiPuAiApi api) {
		return new ZhiPuAiChatModel(api);
	}

	@Bean
	public ZhiPuAiImageModel zhiPuAiImageModel(ZhiPuAiImageApi imageApi) {
		return new ZhiPuAiImageModel(imageApi);
	}

	@Bean
	public EmbeddingModel zhiPuAiEmbeddingModel(ZhiPuAiApi api) {
		return new ZhiPuAiEmbeddingModel(api);
	}

}
