package org.springframework.ai.qianfan;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class QianFanTestConfiguration {

	@Bean
	public QianFanApi qianFanApi() {
		return new QianFanApi(getApiKey(), getSecretKey());
	}

	@Bean
	public QianFanImageApi qianFanImageApi() {
		return new QianFanImageApi(getApiKey(), getSecretKey());
	}

	private String getApiKey() {
		String apiKey = System.getenv("QIANFAN_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key. Put it in an environment variable under the name QIANFAN_API_KEY");
		}
		return apiKey;
	}

	private String getSecretKey() {
		String apiKey = System.getenv("QIANFAN_SECRET_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide a secret key. Put it in an environment variable under the name QIANFAN_SECRET_KEY");
		}
		return apiKey;
	}

	@Bean
	public QianFanChatModel qianFanChatModel(QianFanApi api) {
		return new QianFanChatModel(api);
	}

	@Bean
	public EmbeddingModel qianFanEmbeddingModel(QianFanApi api) {
		return new QianFanEmbeddingModel(api);
	}

	@Bean
	public ImageModel qianFanImageModel(QianFanImageApi api) {
		return new QianFanImageModel(api);
	}

}
