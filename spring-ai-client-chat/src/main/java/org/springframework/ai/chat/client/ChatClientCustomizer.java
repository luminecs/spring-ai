package org.springframework.ai.chat.client;

@FunctionalInterface
public interface ChatClientCustomizer {

	void customize(ChatClient.Builder chatClientBuilder);

}
