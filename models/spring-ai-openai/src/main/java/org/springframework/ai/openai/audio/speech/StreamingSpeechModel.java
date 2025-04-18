package org.springframework.ai.openai.audio.speech;

import reactor.core.publisher.Flux;

import org.springframework.ai.model.StreamingModel;

@FunctionalInterface
public interface StreamingSpeechModel extends StreamingModel<SpeechPrompt, SpeechResponse> {

	default Flux<byte[]> stream(String message) {
		SpeechPrompt prompt = new SpeechPrompt(message);
		return stream(prompt).map(SpeechResponse::getResult).map(Speech::getOutput);
	}

	@Override
	Flux<SpeechResponse> stream(SpeechPrompt prompt);

}
