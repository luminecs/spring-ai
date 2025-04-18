package org.springframework.ai.model.chat.client.autoconfigure;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;

public class ChatClientBuilderConfigurer {

	private List<ChatClientCustomizer> customizers;

	void setChatClientCustomizers(List<ChatClientCustomizer> customizers) {
		this.customizers = customizers;
	}

	public ChatClient.Builder configure(ChatClient.Builder builder) {
		applyCustomizers(builder);
		return builder;
	}

	private void applyCustomizers(ChatClient.Builder builder) {
		if (this.customizers != null) {
			for (ChatClientCustomizer customizer : this.customizers) {
				customizer.customize(builder);
			}
		}
	}

}
