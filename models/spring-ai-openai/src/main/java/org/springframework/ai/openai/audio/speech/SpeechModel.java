package org.springframework.ai.openai.audio.speech;

import org.springframework.ai.model.Model;

@FunctionalInterface
public interface SpeechModel extends Model<SpeechPrompt, SpeechResponse> {

	default byte[] call(String message) {
		SpeechPrompt prompt = new SpeechPrompt(message);
		return call(prompt).getResult().getOutput();
	}

	SpeechResponse call(SpeechPrompt request);

}
