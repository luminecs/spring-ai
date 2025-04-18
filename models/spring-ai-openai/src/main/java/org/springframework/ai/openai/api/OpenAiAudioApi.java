package org.springframework.ai.openai.api;

import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class OpenAiAudioApi {

	private final RestClient restClient;

	private final WebClient webClient;

	public OpenAiAudioApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers,
			RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> authHeaders = h -> {
			if (!(apiKey instanceof NoopApiKey)) {
				h.setBearerAuth(apiKey.getValue());
			}
			h.addAll(headers);

		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(authHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();

		this.webClient = webClientBuilder.baseUrl(baseUrl).defaultHeaders(authHeaders).build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public ResponseEntity<byte[]> createSpeech(SpeechRequest requestBody) {
		return this.restClient.post().uri("/v1/audio/speech").body(requestBody).retrieve().toEntity(byte[].class);
	}

	public Flux<ResponseEntity<byte[]>> stream(SpeechRequest requestBody) {

		return this.webClient.post()
			.uri("/v1/audio/speech")
			.body(Mono.just(requestBody), SpeechRequest.class)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.exchangeToFlux(clientResponse -> {
				HttpHeaders headers = clientResponse.headers().asHttpHeaders();
				return clientResponse.bodyToFlux(byte[].class)
					.map(bytes -> ResponseEntity.ok().headers(headers).body(bytes));
			});
	}

	public ResponseEntity<?> createTranscription(TranscriptionRequest requestBody) {
		return createTranscription(requestBody, requestBody.responseFormat().getResponseType());
	}

	public <T> ResponseEntity<T> createTranscription(TranscriptionRequest requestBody, Class<T> responseType) {

		MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
		multipartBody.add("file", new ByteArrayResource(requestBody.file()) {

			@Override
			public String getFilename() {
				return "audio.webm";
			}
		});
		multipartBody.add("model", requestBody.model());
		multipartBody.add("language", requestBody.language());
		multipartBody.add("prompt", requestBody.prompt());
		multipartBody.add("response_format", requestBody.responseFormat().getValue());
		multipartBody.add("temperature", requestBody.temperature());
		if (requestBody.granularityType() != null) {
			Assert.isTrue(requestBody.responseFormat() == TranscriptResponseFormat.VERBOSE_JSON,
					"response_format must be set to verbose_json to use timestamp granularities.");
			multipartBody.add("timestamp_granularities[]", requestBody.granularityType().getValue());
		}

		return this.restClient.post()
			.uri("/v1/audio/transcriptions")
			.body(multipartBody)
			.retrieve()
			.toEntity(responseType);
	}

	public ResponseEntity<?> createTranslation(TranslationRequest requestBody) {
		return createTranslation(requestBody, requestBody.responseFormat().getResponseType());
	}

	public <T> ResponseEntity<T> createTranslation(TranslationRequest requestBody, Class<T> responseType) {

		MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
		multipartBody.add("file", new ByteArrayResource(requestBody.file()) {

			@Override
			public String getFilename() {
				return "audio.webm";
			}
		});
		multipartBody.add("model", requestBody.model());
		multipartBody.add("prompt", requestBody.prompt());
		multipartBody.add("response_format", requestBody.responseFormat().getValue());
		multipartBody.add("temperature", requestBody.temperature());

		return this.restClient.post()
			.uri("/v1/audio/translations")
			.body(multipartBody)
			.retrieve()
			.toEntity(responseType);
	}

	public enum TtsModel {

		// @formatter:off

		@JsonProperty("tts-1")
		TTS_1("tts-1"),

		@JsonProperty("tts-1-hd")
		TTS_1_HD("tts-1-hd");
		// @formatter:on

		public final String value;

		TtsModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}

	public enum WhisperModel {

		// @formatter:off
		@JsonProperty("whisper-1")
		WHISPER_1("whisper-1");
		// @formatter:on

		public final String value;

		WhisperModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}

	public enum TranscriptResponseFormat {

		// @formatter:off
		@JsonProperty("json")
		JSON("json", StructuredResponse.class),
		@JsonProperty("text")
		TEXT("text", String.class),
		@JsonProperty("srt")
		SRT("srt", String.class),
		@JsonProperty("verbose_json")
		VERBOSE_JSON("verbose_json", StructuredResponse.class),
		@JsonProperty("vtt")
		VTT("vtt", String.class);
		// @formatter:on

		public final String value;

		public final Class<?> responseType;

		TranscriptResponseFormat(String value, Class<?> responseType) {
			this.value = value;
			this.responseType = responseType;
		}

		public boolean isJsonType() {
			return this == JSON || this == VERBOSE_JSON;
		}

		public String getValue() {
			return this.value;
		}

		public Class<?> getResponseType() {
			return this.responseType;
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record SpeechRequest(
	// @formatter:off
		@JsonProperty("model") String model,
		@JsonProperty("input") String input,
		@JsonProperty("voice") String voice,
		@JsonProperty("response_format") AudioResponseFormat responseFormat,
		@JsonProperty("speed") Float speed) {
		// @formatter:on

		public static Builder builder() {
			return new Builder();
		}

		public enum Voice {

			// @formatter:off
			@JsonProperty("alloy")
			ALLOY("alloy"),
			@JsonProperty("echo")
			ECHO("echo"),
			@JsonProperty("fable")
			FABLE("fable"),
			@JsonProperty("onyx")
			ONYX("onyx"),
			@JsonProperty("nova")
			NOVA("nova"),
			@JsonProperty("shimmer")
			SHIMMER("shimmer"),
			@JsonProperty("sage")
			SAGE("sage"),
			@JsonProperty("coral")
			CORAL("coral"),
			@JsonProperty("ash")
			ASH("ash");
			// @formatter:on

			public final String value;

			Voice(String value) {
				this.value = value;
			}

			public String getValue() {
				return this.value;
			}

		}

		public enum AudioResponseFormat {

			// @formatter:off
			@JsonProperty("mp3")
			MP3("mp3"),
			@JsonProperty("opus")
			OPUS("opus"),
			@JsonProperty("aac")
			AAC("aac"),
			@JsonProperty("flac")
			FLAC("flac"),
			@JsonProperty("wav")
			WAV("wav"),
			@JsonProperty("pcm")
			PCM("pcm");
			// @formatter:on

			public final String value;

			AudioResponseFormat(String value) {
				this.value = value;
			}

			public String getValue() {
				return this.value;
			}

		}

		public static class Builder {

			private String model = TtsModel.TTS_1.getValue();

			private String input;

			private String voice;

			private AudioResponseFormat responseFormat = AudioResponseFormat.MP3;

			private Float speed;

			public Builder model(String model) {
				this.model = model;
				return this;
			}

			public Builder input(String input) {
				this.input = input;
				return this;
			}

			public Builder voice(String voice) {
				this.voice = voice;
				return this;
			}

			public Builder voice(Voice voice) {
				this.voice = voice.getValue();
				return this;
			}

			public Builder responseFormat(AudioResponseFormat responseFormat) {
				this.responseFormat = responseFormat;
				return this;
			}

			public Builder speed(Float speed) {
				this.speed = speed;
				return this;
			}

			public SpeechRequest build() {
				Assert.hasText(this.model, "model must not be empty");
				Assert.hasText(this.input, "input must not be empty");

				return new SpeechRequest(this.model, this.input, this.voice, this.responseFormat, this.speed);
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record TranscriptionRequest(
	// @formatter:off
		@JsonProperty("file") byte[] file,
		@JsonProperty("model") String model,
		@JsonProperty("language") String language,
		@JsonProperty("prompt") String prompt,
		@JsonProperty("response_format") TranscriptResponseFormat responseFormat,
		@JsonProperty("temperature") Float temperature,
		@JsonProperty("timestamp_granularities") GranularityType granularityType) {
		// @formatter:on

		public static Builder builder() {
			return new Builder();
		}

		public enum GranularityType {

			// @formatter:off
			@JsonProperty("word")
			WORD("word"),
			@JsonProperty("segment")
			SEGMENT("segment");
			// @formatter:on

			public final String value;

			GranularityType(String value) {
				this.value = value;
			}

			public String getValue() {
				return this.value;
			}

		}

		public static class Builder {

			private byte[] file;

			private String model = WhisperModel.WHISPER_1.getValue();

			private String language;

			private String prompt;

			private TranscriptResponseFormat responseFormat = TranscriptResponseFormat.JSON;

			private Float temperature;

			private GranularityType granularityType;

			public Builder file(byte[] file) {
				this.file = file;
				return this;
			}

			public Builder model(String model) {
				this.model = model;
				return this;
			}

			public Builder language(String language) {
				this.language = language;
				return this;
			}

			public Builder prompt(String prompt) {
				this.prompt = prompt;
				return this;
			}

			public Builder responseFormat(TranscriptResponseFormat responseFormat) {
				this.responseFormat = responseFormat;
				return this;
			}

			public Builder temperature(Float temperature) {
				this.temperature = temperature;
				return this;
			}

			public Builder granularityType(GranularityType granularityType) {
				this.granularityType = granularityType;
				return this;
			}

			public TranscriptionRequest build() {
				Assert.notNull(this.file, "file must not be null");
				Assert.hasText(this.model, "model must not be empty");
				Assert.notNull(this.responseFormat, "response_format must not be null");

				return new TranscriptionRequest(this.file, this.model, this.language, this.prompt, this.responseFormat,
						this.temperature, this.granularityType);
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record TranslationRequest(
	// @formatter:off
		@JsonProperty("file") byte[] file,
		@JsonProperty("model") String model,
		@JsonProperty("prompt") String prompt,
		@JsonProperty("response_format") TranscriptResponseFormat responseFormat,
		@JsonProperty("temperature") Float temperature) {
		// @formatter:on

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private byte[] file;

			private String model = WhisperModel.WHISPER_1.getValue();

			private String prompt;

			private TranscriptResponseFormat responseFormat = TranscriptResponseFormat.JSON;

			private Float temperature;

			public Builder file(byte[] file) {
				this.file = file;
				return this;
			}

			public Builder model(String model) {
				this.model = model;
				return this;
			}

			public Builder prompt(String prompt) {
				this.prompt = prompt;
				return this;
			}

			public Builder responseFormat(TranscriptResponseFormat responseFormat) {
				this.responseFormat = responseFormat;
				return this;
			}

			public Builder temperature(Float temperature) {
				this.temperature = temperature;
				return this;
			}

			public TranslationRequest build() {
				Assert.notNull(this.file, "file must not be null");
				Assert.hasText(this.model, "model must not be empty");
				Assert.notNull(this.responseFormat, "response_format must not be null");

				return new TranslationRequest(this.file, this.model, this.prompt, this.responseFormat,
						this.temperature);
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record StructuredResponse(
	// @formatter:off
		@JsonProperty("language") String language,
		@JsonProperty("duration") Float duration,
		@JsonProperty("text") String text,
		@JsonProperty("words") List<Word> words,
		@JsonProperty("segments") List<Segment> segments) {
		// @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record Word(
		// @formatter:off
			@JsonProperty("word") String word,
			@JsonProperty("start") Float start,
			@JsonProperty("end") Float end) {
			// @formatter:on
		}

		@JsonInclude(Include.NON_NULL)
		public record Segment(
		// @formatter:off
			@JsonProperty("id") Integer id,
			@JsonProperty("seek") Integer seek,
			@JsonProperty("start") Float start,
			@JsonProperty("end") Float end,
			@JsonProperty("text") String text,
			@JsonProperty("tokens") List<Integer> tokens,
			@JsonProperty("temperature") Float temperature,
			@JsonProperty("avg_logprob") Float avgLogprob,
			@JsonProperty("compression_ratio") Float compressionRatio,
			@JsonProperty("no_speech_prob") Float noSpeechProb) {
			// @formatter:on
		}

	}

	public static class Builder {

		private String baseUrl = OpenAiApiConstants.DEFAULT_BASE_URL;

		private ApiKey apiKey;

		private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

		private RestClient.Builder restClientBuilder = RestClient.builder();

		private WebClient.Builder webClientBuilder = WebClient.builder();

		private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

		public Builder baseUrl(String baseUrl) {
			Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
			this.baseUrl = baseUrl;
			return this;
		}

		public Builder apiKey(ApiKey apiKey) {
			Assert.notNull(apiKey, "apiKey cannot be null");
			this.apiKey = apiKey;
			return this;
		}

		public Builder apiKey(String simpleApiKey) {
			Assert.notNull(simpleApiKey, "simpleApiKey cannot be null");
			this.apiKey = new SimpleApiKey(simpleApiKey);
			return this;
		}

		public Builder headers(MultiValueMap<String, String> headers) {
			Assert.notNull(headers, "headers cannot be null");
			this.headers = headers;
			return this;
		}

		public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
			Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
			this.restClientBuilder = restClientBuilder;
			return this;
		}

		public Builder webClientBuilder(WebClient.Builder webClientBuilder) {
			Assert.notNull(webClientBuilder, "webClientBuilder cannot be null");
			this.webClientBuilder = webClientBuilder;
			return this;
		}

		public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
			Assert.notNull(responseErrorHandler, "responseErrorHandler cannot be null");
			this.responseErrorHandler = responseErrorHandler;
			return this;
		}

		public OpenAiAudioApi build() {
			Assert.notNull(this.apiKey, "apiKey must be set");
			return new OpenAiAudioApi(this.baseUrl, this.apiKey, this.headers, this.restClientBuilder,
					this.webClientBuilder, this.responseErrorHandler);
		}

	}

}
