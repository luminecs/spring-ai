package org.springframework.ai.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.StructuredResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioTranscriptionResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class OpenAiAudioTranscriptionModel implements Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final OpenAiAudioTranscriptionOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final OpenAiAudioApi audioApi;

	public OpenAiAudioTranscriptionModel(OpenAiAudioApi audioApi) {
		this(audioApi,
				OpenAiAudioTranscriptionOptions.builder()
					.model(OpenAiAudioApi.WhisperModel.WHISPER_1.getValue())
					.responseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
					.temperature(0.7f)
					.build());
	}

	public OpenAiAudioTranscriptionModel(OpenAiAudioApi audioApi, OpenAiAudioTranscriptionOptions options) {
		this(audioApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public OpenAiAudioTranscriptionModel(OpenAiAudioApi audioApi, OpenAiAudioTranscriptionOptions options,
			RetryTemplate retryTemplate) {
		Assert.notNull(audioApi, "OpenAiAudioApi must not be null");
		Assert.notNull(options, "OpenAiTranscriptionOptions must not be null");
		Assert.notNull(retryTemplate, "RetryTemplate must not be null");
		this.audioApi = audioApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
	}

	public String call(Resource audioResource) {
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioResource);
		return call(transcriptionRequest).getResult().getOutput();
	}

	@Override
	public AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt) {

		Resource audioResource = transcriptionPrompt.getInstructions();

		OpenAiAudioApi.TranscriptionRequest request = createRequest(transcriptionPrompt);

		if (request.responseFormat().isJsonType()) {

			ResponseEntity<StructuredResponse> transcriptionEntity = this.retryTemplate
				.execute(ctx -> this.audioApi.createTranscription(request, StructuredResponse.class));

			var transcription = transcriptionEntity.getBody();

			if (transcription == null) {
				logger.warn("No transcription returned for request: {}", audioResource);
				return new AudioTranscriptionResponse(null);
			}

			AudioTranscription transcript = new AudioTranscription(transcription.text());

			RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(transcriptionEntity);

			return new AudioTranscriptionResponse(transcript,
					OpenAiAudioTranscriptionResponseMetadata.from(transcriptionEntity.getBody())
						.withRateLimit(rateLimits));

		}
		else {

			ResponseEntity<String> transcriptionEntity = this.retryTemplate
				.execute(ctx -> this.audioApi.createTranscription(request, String.class));

			var transcription = transcriptionEntity.getBody();

			if (transcription == null) {
				logger.warn("No transcription returned for request: {}", audioResource);
				return new AudioTranscriptionResponse(null);
			}

			AudioTranscription transcript = new AudioTranscription(transcription);

			RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(transcriptionEntity);

			return new AudioTranscriptionResponse(transcript,
					OpenAiAudioTranscriptionResponseMetadata.from(transcriptionEntity.getBody())
						.withRateLimit(rateLimits));
		}
	}

	OpenAiAudioApi.TranscriptionRequest createRequest(AudioTranscriptionPrompt transcriptionPrompt) {

		OpenAiAudioTranscriptionOptions options = this.defaultOptions;

		if (transcriptionPrompt.getOptions() != null) {
			if (transcriptionPrompt.getOptions() instanceof OpenAiAudioTranscriptionOptions runtimeOptions) {
				options = this.merge(runtimeOptions, options);
			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type TranscriptionOptions: "
						+ transcriptionPrompt.getOptions().getClass().getSimpleName());
			}
		}

		return OpenAiAudioApi.TranscriptionRequest.builder()
			.file(toBytes(transcriptionPrompt.getInstructions()))
			.responseFormat(options.getResponseFormat())
			.prompt(options.getPrompt())
			.temperature(options.getTemperature())
			.language(options.getLanguage())
			.model(options.getModel())
			.granularityType(options.getGranularityType())
			.build();
	}

	private byte[] toBytes(Resource resource) {
		try {
			return resource.getInputStream().readAllBytes();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Failed to read resource: " + resource, e);
		}
	}

	private OpenAiAudioTranscriptionOptions merge(OpenAiAudioTranscriptionOptions source,
			OpenAiAudioTranscriptionOptions target) {

		if (source == null) {
			source = new OpenAiAudioTranscriptionOptions();
		}

		OpenAiAudioTranscriptionOptions merged = new OpenAiAudioTranscriptionOptions();
		merged.setLanguage(source.getLanguage() != null ? source.getLanguage() : target.getLanguage());
		merged.setModel(source.getModel() != null ? source.getModel() : target.getModel());
		merged.setPrompt(source.getPrompt() != null ? source.getPrompt() : target.getPrompt());
		merged.setResponseFormat(
				source.getResponseFormat() != null ? source.getResponseFormat() : target.getResponseFormat());
		merged.setTemperature(source.getTemperature() != null ? source.getTemperature() : target.getTemperature());
		merged.setGranularityType(
				source.getGranularityType() != null ? source.getGranularityType() : target.getGranularityType());
		return merged;
	}

}
