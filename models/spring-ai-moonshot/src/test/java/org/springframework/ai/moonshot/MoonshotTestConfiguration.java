package org.springframework.ai.moonshot;

import org.springframework.ai.moonshot.api.MoonshotApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootConfiguration
public class MoonshotTestConfiguration {

	@Bean
	public MoonshotApi moonshotApi() {
		var apiKey = System.getenv("MOONSHOT_API_KEY");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalArgumentException(
					"Missing MOONSHOT_API_KEY environment variable. Please set it to your Moonshot API key.");
		}
		return new MoonshotApi(apiKey);
	}

	@Bean
	public MoonshotChatModel moonshotChatModel(MoonshotApi moonshotApi) {
		return new MoonshotChatModel(moonshotApi);
	}

	public void tst() {
	}

}
