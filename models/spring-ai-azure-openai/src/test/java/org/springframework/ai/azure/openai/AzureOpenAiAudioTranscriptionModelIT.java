package org.springframework.ai.azure.openai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AzureOpenAiAudioTranscriptionModelIT.TestConfiguration.class)
@EnabledIfEnvironmentVariables({
		@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_TRANSCRIPTION_API_KEY", matches = ".+"),
		@EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_TRANSCRIPTION_ENDPOINT", matches = ".+") })
class AzureOpenAiAudioTranscriptionModelIT {

	@Value("classpath:/speech/jfk.flac")
	private Resource audioFile;

	@Autowired
	private AzureOpenAiAudioTranscriptionModel transcriptionModel;

	@Test
	void transcriptionTest() {
		AzureOpenAiAudioTranscriptionOptions transcriptionOptions = AzureOpenAiAudioTranscriptionOptions.builder()
			.responseFormat(AzureOpenAiAudioTranscriptionOptions.TranscriptResponseFormat.TEXT)
			.temperature(0f)
			.build();
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile,
				transcriptionOptions);
		AudioTranscriptionResponse response = this.transcriptionModel.call(transcriptionRequest);
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().toLowerCase().contains("fellow")).isTrue();
	}

	@Test
	void transcriptionTestWithOptions() {
		AzureOpenAiAudioTranscriptionOptions.TranscriptResponseFormat responseFormat = AzureOpenAiAudioTranscriptionOptions.TranscriptResponseFormat.VTT;

		AzureOpenAiAudioTranscriptionOptions transcriptionOptions = AzureOpenAiAudioTranscriptionOptions.builder()
			.language("en")
			.prompt("Ask not this, but ask that")
			.temperature(0f)
			.responseFormat(responseFormat)
			.build();
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile,
				transcriptionOptions);
		AudioTranscriptionResponse response = this.transcriptionModel.call(transcriptionRequest);
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().toLowerCase().contains("fellow")).isTrue();
	}

	@SpringBootConfiguration
	public static class TestConfiguration {

		@Bean
		public OpenAIClient openAIClient() {
			String apiKey = System.getenv("AZURE_OPENAI_TRANSCRIPTION_API_KEY");
			String endpoint = System.getenv("AZURE_OPENAI_TRANSCRIPTION_ENDPOINT");

			return new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey))
				.endpoint(endpoint)

				.buildClient();
		}

		@Bean
		public AzureOpenAiAudioTranscriptionModel azureOpenAiChatModel(OpenAIClient openAIClient) {
			return new AzureOpenAiAudioTranscriptionModel(openAIClient,
					AzureOpenAiAudioTranscriptionOptions.builder().deploymentName("whisper").build());
		}

	}

}
