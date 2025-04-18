package org.springframework.ai.model;

public final class SpringAIModelProperties {

	private SpringAIModelProperties() {

	}

	public static final String MODEL_PREFIX = "spring.ai.model";

	public static final String CHAT_MODEL = MODEL_PREFIX + ".chat";

	public static final String EMBEDDING_MODEL = MODEL_PREFIX + ".embedding";

	public static final String TEXT_EMBEDDING_MODEL = MODEL_PREFIX + ".embedding.text";

	public static final String MULTI_MODAL_EMBEDDING_MODEL = MODEL_PREFIX + ".embedding.multimodal";

	public static final String IMAGE_MODEL = MODEL_PREFIX + ".image";

	public static final String AUDIO_TRANSCRIPTION_MODEL = MODEL_PREFIX + ".audio.transcription";

	public static final String AUDIO_SPEECH_MODEL = MODEL_PREFIX + ".audio.speech";

	public static final String MODERATION_MODEL = MODEL_PREFIX + ".moderation";

}
