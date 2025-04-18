package org.springframework.ai.audio.transcription;

import org.springframework.ai.model.ModelOptions;

public interface AudioTranscriptionOptions extends ModelOptions {

	String getModel();

}
