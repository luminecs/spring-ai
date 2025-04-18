package org.springframework.ai.huggingface;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class HuggingfaceTestConfiguration {

	@Bean
	public HuggingfaceChatModel huggingfaceChatModel() {
		String apiKey = System.getenv("HUGGINGFACE_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"You must provide an API key.  Put it in an environment variable under the name HUGGINGFACE_API_KEY");
		}

		HuggingfaceChatModel huggingfaceChatModel = new HuggingfaceChatModel(apiKey,
				System.getenv("HUGGINGFACE_CHAT_URL"));
		return huggingfaceChatModel;
	}

}
