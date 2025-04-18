package org.springframework.ai.openai.audio.transcription;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class TranscriptionModelTests {

	@Test
	void transcrbeRequestReturnsResponseCorrectly() {

		Resource mockAudioFile = Mockito.mock(Resource.class);

		OpenAiAudioTranscriptionModel mockClient = Mockito.mock(OpenAiAudioTranscriptionModel.class);

		String mockTranscription = "All your bases are belong to us";

		AudioTranscription transcript = Mockito.mock(AudioTranscription.class);
		given(transcript.getOutput()).willReturn(mockTranscription);

		AudioTranscriptionResponse response = Mockito.mock(AudioTranscriptionResponse.class);
		given(response.getResult()).willReturn(transcript);

		doCallRealMethod().when(mockClient).call(any(Resource.class));

		given(mockClient.call(any(AudioTranscriptionPrompt.class))).will(invocation -> {
			AudioTranscriptionPrompt transcriptionRequest = invocation.getArgument(0);

			assertThat(transcriptionRequest).isNotNull();
			assertThat(transcriptionRequest.getInstructions()).isEqualTo(mockAudioFile);

			return response;
		});

		assertThat(mockClient.call(mockAudioFile)).isEqualTo(mockTranscription);

		verify(mockClient, times(1)).call(eq(mockAudioFile));
		verify(mockClient, times(1)).call(isA(AudioTranscriptionPrompt.class));
		verify(response, times(1)).getResult();
		verify(transcript, times(1)).getOutput();
		verifyNoMoreInteractions(mockClient, transcript, response);
	}

}
