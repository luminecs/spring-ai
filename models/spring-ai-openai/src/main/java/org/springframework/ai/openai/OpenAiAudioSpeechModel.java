package org.springframework.ai.openai;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.ai.openai.audio.speech.Speech;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.audio.speech.StreamingSpeechModel;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class OpenAiAudioSpeechModel implements SpeechModel, StreamingSpeechModel {

	private static final Float SPEED = 1.0f;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final OpenAiAudioSpeechOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final OpenAiAudioApi audioApi;

	public OpenAiAudioSpeechModel(OpenAiAudioApi audioApi) {
		this(audioApi,
				OpenAiAudioSpeechOptions.builder()
					.model(OpenAiAudioApi.TtsModel.TTS_1.getValue())
					.responseFormat(AudioResponseFormat.MP3)
					.voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY.getValue())
					.speed(SPEED)
					.build());
	}

	public OpenAiAudioSpeechModel(OpenAiAudioApi audioApi, OpenAiAudioSpeechOptions options) {
		this(audioApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public OpenAiAudioSpeechModel(OpenAiAudioApi audioApi, OpenAiAudioSpeechOptions options,
			RetryTemplate retryTemplate) {
		Assert.notNull(audioApi, "OpenAiAudioApi must not be null");
		Assert.notNull(options, "OpenAiSpeechOptions must not be null");
		Assert.notNull(options, "RetryTemplate must not be null");
		this.audioApi = audioApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
	}

	@Override
	public byte[] call(String text) {
		SpeechPrompt speechRequest = new SpeechPrompt(text);
		return call(speechRequest).getResult().getOutput();
	}

	@Override
	public SpeechResponse call(SpeechPrompt speechPrompt) {

		OpenAiAudioApi.SpeechRequest speechRequest = createRequest(speechPrompt);

		ResponseEntity<byte[]> speechEntity = this.retryTemplate
			.execute(ctx -> this.audioApi.createSpeech(speechRequest));

		var speech = speechEntity.getBody();

		if (speech == null) {
			logger.warn("No speech response returned for speechRequest: {}", speechRequest);
			return new SpeechResponse(new Speech(new byte[0]));
		}

		RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(speechEntity);

		return new SpeechResponse(new Speech(speech), new OpenAiAudioSpeechResponseMetadata(rateLimits));
	}

	@Override
	public Flux<SpeechResponse> stream(SpeechPrompt speechPrompt) {

		OpenAiAudioApi.SpeechRequest speechRequest = createRequest(speechPrompt);

		Flux<ResponseEntity<byte[]>> speechEntity = this.retryTemplate
			.execute(ctx -> this.audioApi.stream(speechRequest));

		return speechEntity.map(entity -> new SpeechResponse(new Speech(entity.getBody()),
				new OpenAiAudioSpeechResponseMetadata(OpenAiResponseHeaderExtractor.extractAiResponseHeaders(entity))));
	}

	private OpenAiAudioApi.SpeechRequest createRequest(SpeechPrompt request) {
		OpenAiAudioSpeechOptions options = this.defaultOptions;

		if (request.getOptions() != null) {
			if (request.getOptions() instanceof OpenAiAudioSpeechOptions runtimeOptions) {
				options = this.merge(runtimeOptions, options);
			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type SpeechOptions: "
						+ request.getOptions().getClass().getSimpleName());
			}
		}

		String input = StringUtils.isNotBlank(options.getInput()) ? options.getInput()
				: request.getInstructions().getText();

		OpenAiAudioApi.SpeechRequest.Builder requestBuilder = OpenAiAudioApi.SpeechRequest.builder()
			.model(options.getModel())
			.input(input)
			.voice(options.getVoice())
			.responseFormat(options.getResponseFormat())
			.speed(options.getSpeed());

		return requestBuilder.build();
	}

	private OpenAiAudioSpeechOptions merge(OpenAiAudioSpeechOptions source, OpenAiAudioSpeechOptions target) {
		OpenAiAudioSpeechOptions.Builder mergedBuilder = OpenAiAudioSpeechOptions.builder();

		mergedBuilder.model(source.getModel() != null ? source.getModel() : target.getModel());
		mergedBuilder.input(source.getInput() != null ? source.getInput() : target.getInput());
		mergedBuilder.voice(source.getVoice() != null ? source.getVoice() : target.getVoice());
		mergedBuilder.responseFormat(
				source.getResponseFormat() != null ? source.getResponseFormat() : target.getResponseFormat());
		mergedBuilder.speed(source.getSpeed() != null ? source.getSpeed() : target.getSpeed());

		return mergedBuilder.build();
	}

}
