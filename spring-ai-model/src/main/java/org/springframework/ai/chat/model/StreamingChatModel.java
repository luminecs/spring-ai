package org.springframework.ai.chat.model;

import java.util.Arrays;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.StreamingModel;

@FunctionalInterface
public interface StreamingChatModel extends StreamingModel<Prompt, ChatResponse> {

	default Flux<String> stream(String message) {
		Prompt prompt = new Prompt(message);
		return stream(prompt).map(response -> (response.getResult() == null || response.getResult().getOutput() == null
				|| response.getResult().getOutput().getText() == null) ? ""
						: response.getResult().getOutput().getText());
	}

	default Flux<String> stream(Message... messages) {
		Prompt prompt = new Prompt(Arrays.asList(messages));
		return stream(prompt).map(response -> (response.getResult() == null || response.getResult().getOutput() == null
				|| response.getResult().getOutput().getText() == null) ? ""
						: response.getResult().getOutput().getText());
	}

	@Override
	Flux<ChatResponse> stream(Prompt prompt);

}
